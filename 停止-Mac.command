#!/bin/bash
set -euo pipefail
export PATH="/usr/local/bin:/opt/homebrew/bin:$PATH"
cd "$(dirname "$0")"
trap 'echo "停止失败，请查看上方错误。"; read -r -p "按回车退出…"' ERR
docker compose stop
echo "平台已停止，数据库和上传文件已保留。"
read -r -p "按回车关闭窗口…"
