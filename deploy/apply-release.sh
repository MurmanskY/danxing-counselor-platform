#!/usr/bin/env bash
set -Eeuo pipefail
release=${1:?Pass the uploaded release directory}
root=/opt/danxing
release=$(realpath "$release")
[[ "$release" == "$root/releases/"* && -f "$release/release.env" ]] || exit 2
mkdir -p "$root/shared"
exec 9>"$root/shared/deploy.lock"
flock -w 600 9
if [[ ! -f "$root/shared/.env" ]]; then
  (cd "$release"; python3 scripts/init-env.py)
  mv "$release/.env" "$root/shared/.env"
  sed -i 's/^WEB_PORT=.*/WEB_PORT=80/' "$root/shared/.env"
fi
chmod 600 "$root/shared/.env"
previous=$(readlink -f "$root/current" || true)
compose() {
  local dir=$1; shift
  docker compose --project-name danxing --env-file "$root/shared/.env" --env-file "$dir/release.env" -f "$dir/compose.yml" -f "$dir/deploy/compose.ecs.yml" "$@"
}
if [[ -n "$previous" && -f "$previous/release.env" ]]; then
  mkdir -p "$root/shared/backups"
  backup="$root/shared/backups/$(date -u +%Y%m%dT%H%M%SZ).dump"
  umask 077
  compose "$previous" exec -T postgres pg_dump -U danxing -d danxing -Fc > "$backup"
  echo "Database backup saved before deployment."
fi
echo "Loading release images…"
gzip -dc "$release/images.tar.gz" | docker load
if compose "$release" up -d --no-build --pull never --wait --wait-timeout 300; then
  if ! python3 "$release/deploy/verify.py" --env-file "$root/shared/.env"; then
    status=1
  else
    status=0
  fi
else
  status=1
fi
if [[ "$status" != 0 ]]; then
  compose "$release" logs --tail=80 backend frontend
  if [[ -n "$previous" && "$previous" != "$release" && -f "$previous/release.env" ]]; then
    echo "Health check failed; restoring previous application images."
    compose "$previous" up -d --no-build --pull never --wait --wait-timeout 300
  fi
  exit 1
fi
ln -sfn "$release" "$root/current.next"
mv -Tf "$root/current.next" "$root/current"
rm -f "$release/images.tar.gz"
compose "$release" ps
# Keep the last two successful release directories/images. No volume pruning.
if [[ -n "$previous" ]]; then
  for candidate in "$root"/releases/*; do
    [[ -d "$candidate" && "$candidate" != "$release" && "$candidate" != "$previous" ]] || continue
    if [[ -f "$candidate/release.env" ]]; then
      oldtag=$(sed -n 's/^DANXING_IMAGE_TAG=//p' "$candidate/release.env")
      if [[ "$oldtag" =~ ^[a-f0-9]{7,40}$ ]]; then
        docker image rm "danxing-cloud-backend:$oldtag" "danxing-cloud-frontend:$oldtag" "danxing-cloud-minio:$oldtag" >/dev/null 2>&1 || true
        rm -rf -- "$candidate"
      fi
    fi
  done
fi
echo "Deployment verified: $release"
