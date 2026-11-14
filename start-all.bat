@echo off
chcp 65001 >nul
echo ======================================================
echo 🚀 STARTING SCOLARSIA EMULATOR & BACKEND SERVICES
echo ======================================================

echo.
echo 📦 [1/3] Menjalankan Firebase Emulator Suite di Docker (C:\firebase-docker)...
cd /d C:\firebase-docker
docker compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Gagal menjalankan Docker Compose. Pastikan Docker Desktop aktif.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ⏳ [2/3] Menunggu Firebase Emulator siap...
timeout /t 5 /nobreak >nul

echo.
echo 🧹 [3/3] Memeriksa dan membersihkan port 3000...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000 ^| findstr LISTENING 2^>nul') do (
    echo ⚠️ Menutup proses lama di port 3000 (PID: %%a)...
    taskkill /f /pid %%a >nul 2>&1
)

echo.
echo 🚀 Memulai Backend AI Node.js Express (port 3000)...
cd /d %~dp0backend
npm start
