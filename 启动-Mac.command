#!/bin/bash
set -euo pipefail
export PATH="/usr/local/bin:/opt/homebrew/bin:$PATH"
cd "$(dirname "$0")"
trap 'echo "启动失败，请查看上方错误。"; read -r -p "按回车退出…"' ERR
command -v docker >/dev/null || { echo "请先安装 Docker Desktop"; exit 1; }
if ! docker info >/dev/null 2>&1; then
  open -a Docker
  echo "正在等待 Docker Desktop…"
  for ((i=0; i<90; i++)); do
    docker info >/dev/null 2>&1 && break
    sleep 2
  done
fi
docker info >/dev/null
if [ ! -f .env ]; then python3 scripts/init-env.py; fi
echo "正在构建并启动；首次下载依赖可能需要较长时间…"
docker compose up -d --build --wait --wait-timeout 300
port=$(sed -n 's/^WEB_PORT=//p' .env | tr -d '\r')
open "http://localhost:${port:-8080}"
echo "平台已启动。账号 admin，密码见 .env 中 ADMIN_PASSWORD。"
echo "关闭本窗口不会停止服务；停止请双击 停止-Mac.command。"
read -r -p "按回车关闭窗口…"
