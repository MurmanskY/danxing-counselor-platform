#!/usr/bin/env python3
"""Bulk-import user avatars through the admin API.

Photos are matched to accounts by filename: <username>.jpg (or .png), e.g.
photos/counselor-p002.jpg -> account counselor-p002.

Usage:
  ADMIN_USERNAME=admin ADMIN_PASSWORD=... \
  AVATAR_DIR=./photos python3 scripts/import-avatars.py [--base-url http://localhost:8080]

Only .jpg / .jpeg / .png files are uploaded; other files are ignored.
"""
import argparse
import http.cookiejar
import json
import mimetypes
import os
import secrets
import urllib.error
import urllib.parse
import urllib.request

parser = argparse.ArgumentParser()
parser.add_argument("--base-url", default="http://localhost:8080")
parser.add_argument("--avatar-dir", default=os.environ.get("AVATAR_DIR", "./photos"))
args = parser.parse_args()

ADMIN_USERNAME = os.environ.get("ADMIN_USERNAME", "admin")
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD")
if not ADMIN_PASSWORD:
    raise SystemExit("Set ADMIN_PASSWORD in the environment.")

opener = urllib.request.build_opener(
    urllib.request.ProxyHandler({}),
    urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()),
)
csrf = None

def request(method, path, data=None, ctype=None):
    headers = {}
    if csrf and method != "GET":
        headers[csrf["headerName"]] = csrf["token"]
    if data is not None:
        headers["Content-Type"] = ctype
    try:
        with opener.open(
            urllib.request.Request(
                args.base_url.rstrip("/") + "/api" + path,
                data=data, headers=headers, method=method,
            ),
            timeout=60,
        ) as r:
            raw = r.read()
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raise SystemExit(f"{method} {path} -> {e.code}: {e.read()[:200]!r}")

csrf = request("GET", "/auth/csrf")
request(
    "POST", "/auth/login",
    urllib.parse.urlencode(
        {"username": ADMIN_USERNAME, "password": ADMIN_PASSWORD}
    ).encode(),
    "application/x-www-form-urlencoded",
)
csrf = request("GET", "/auth/csrf")

users = request("GET", "/admin/users")
by_username = {u["username"]: u for u in users}

imported, skipped = 0, []
for name in sorted(os.listdir(args.avatar_dir)):
    stem, ext = os.path.splitext(name)
    ext = ext.lower()
    if ext not in (".jpg", ".jpeg", ".png"):
        continue
    user = by_username.get(stem)
    path = os.path.join(args.avatar_dir, name)
    if not user:
        skipped.append(f"{name}（无对应账号 {stem}）")
        continue
    boundary = "AvatarBoundary" + secrets.token_hex(8)
    payload = open(path, "rb").read()
    body = (
        f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; "
        f'filename="{name}"\r\nContent-Type: '
        f"{mimetypes.guess_type(name)[0] or 'application/octet-stream'}\r\n\r\n"
    ).encode() + payload + f"\r\n--{boundary}--\r\n".encode()
    request(
        "POST", f"/admin/users/{urllib.parse.quote(user['id'])}/avatar",
        body, f"multipart/form-data; boundary={boundary}",
    )
    imported += 1
    print(f"OK {name} -> {user['displayName']} ({user['username']})")

print(f"\n完成：{imported} 个头像已上传。")
if skipped:
    print("跳过：")
    for item in skipped:
        print(" -", item)
