@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 正在停止演示环境...
docker compose down
echo 已停止。数据仍保留，下次双击「启动-Windows.bat」可继续使用。
pause
