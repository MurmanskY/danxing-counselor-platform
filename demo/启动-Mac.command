#!/bin/bash
cd "$(dirname "$0")"
echo "=========================================="
echo "  旦行 · 辅导员职业发展数据平台 · Demo"
echo "=========================================="
if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 Docker，请先安装 Docker Desktop："
  echo "https://www.docker.com/products/docker-desktop/"
  open "https://www.docker.com/products/docker-desktop/" 2>/dev/null
  read -n 1 -s -r -p "按任意键退出..."
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker 尚未运行，正在尝试启动 Docker Desktop..."
  open -a "Docker Desktop" 2>/dev/null
  echo "请等待 Docker 完全启动（菜单栏鲸鱼图标静止）后，再次双击本文件。"
  read -n 1 -s -r -p "按任意键退出..."
  exit 1
fi
echo "首次启动需要构建环境（约 10-25 分钟，取决于网速），请耐心等待，不要关闭本窗口。"
echo ""
docker compose up -d --build
if [ $? -ne 0 ]; then
  echo ""
  echo "启动失败，请截图上方信息反馈给管理员。"
  read -n 1 -s -r -p "按任意键退出..."
  exit 1
fi
echo ""
echo "等待服务就绪..."
for i in $(seq 1 72); do
  if curl -sf http://localhost:8080 >/dev/null 2>&1; then
    echo "服务已就绪，正在打开浏览器..."
    sleep 2
    open http://localhost:8080
    echo ""
    echo "完成！测试账号见「使用说明.txt」。"
    echo "如需停止演示，请双击「停止-Mac.command」。"
    read -n 1 -s -r -p "按任意键退出..."
    exit 0
  fi
  sleep 5
done
echo "等待超时，但服务可能仍在启动中，请稍后手动访问 http://localhost:8080"
read -n 1 -s -r -p "按任意键退出..."
