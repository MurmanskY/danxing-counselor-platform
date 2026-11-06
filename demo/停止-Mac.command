#!/bin/bash
cd "$(dirname "$0")"
echo "正在停止演示环境..."
docker compose down
echo "已停止。数据仍保留，下次双击「启动-Mac.command」可继续使用。"
read -n 1 -s -r -p "按任意键退出..."
