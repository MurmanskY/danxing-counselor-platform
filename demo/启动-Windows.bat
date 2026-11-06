@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo   旦行 · 辅导员职业发展数据平台 · Demo
echo ==========================================
docker info >nul 2>nul
if errorlevel 1 (
  echo 未检测到可用的 Docker。
  echo 请先安装并启动 Docker Desktop：
  echo https://www.docker.com/products/docker-desktop/
  start "" "https://www.docker.com/products/docker-desktop/"
  pause
  exit /b 1
)
echo 首次启动需要构建环境（约 10-25 分钟，取决于网速），请耐心等待，不要关闭本窗口。
echo.
docker compose up -d --build
if errorlevel 1 (
  echo.
  echo 启动失败，请截图上方信息反馈给管理员。
  pause
  exit /b 1
)
echo.
echo 等待服务就绪...
for /l %%i in (1,1,72) do (
  powershell -NoProfile -Command "try { (Invoke-WebRequest -UseBasicParsing -TimeoutSec 5 http://localhost:8080).StatusCode } catch { 0 }" 2>nul | findstr "200" >nul
  if not errorlevel 1 goto READY
  timeout /t 5 >nul
)
echo 等待超时，但服务可能仍在启动中，请稍后手动访问 http://localhost:8080
pause
exit /b 0
:READY
echo 服务已就绪，正在打开浏览器...
timeout /t 2 >nul
start "" http://localhost:8080
echo.
echo 完成！测试账号见「使用说明.txt」。
echo 如需停止演示，请双击「停止-Windows.bat」。
pause
