#!/bin/bash
# 打包演示包：生成 dist/danxing-demo.zip，可直接发给测试人员
set -e
cd "$(dirname "$0")/.."
OUT=dist/danxing-demo
rm -rf dist
mkdir -p "$OUT"

echo "复制后端源码..."
tar cf - --exclude=target --exclude=.DS_Store backend | tar xf - -C "$OUT"
echo "复制前端源码..."
tar cf - --exclude=node_modules --exclude=dist --exclude=.DS_Store frontend | tar xf - -C "$OUT"
echo "复制 MinIO 构建文件..."
tar cf - --exclude=.DS_Store infra/minio | tar xf - -C "$OUT"

cp demo/compose.yml "$OUT/compose.yml"
cp demo/demo.env "$OUT/.env"
cp demo/使用说明.txt "$OUT/"
cp "demo/启动-Mac.command" "demo/停止-Mac.command" "$OUT/"
cp "demo/启动-Windows.bat" "demo/停止-Windows.bat" "$OUT/"
chmod +x "$OUT/启动-Mac.command" "$OUT/停止-Mac.command"

echo "生成压缩包..."
(cd dist && zip -qr danxing-demo.zip danxing-demo)
du -sh dist/danxing-demo.zip
echo "完成：dist/danxing-demo.zip"
