@echo off
chcp 65001 >nul
echo ======================================================
echo 🛑 STOPPING SCOLARSIA EMULATOR & BACKEND SERVICES
echo ======================================================

echo.
echo 🧹 [1/2] Menghentikan Backend Node.js di port 3000...
powershell -Command "Get-Process -Id (Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue).OwningProcess -ErrorAction SilentlyContinue | Stop-Process -Force"

echo.
echo 📦 [2/2] Menghentikan Firebase Emulator (docker compose stop)...
cd /d C:\firebase-docker
docker compose stop

echo.
echo ⏳ Memeriksa berkas hasil export di ./emulator-data/export...
dir /b C:\firebase-docker\emulator-data\export 2>nul

echo.
echo ✅ Semua layanan berhasil dihentikan dan data emulator tersimpan secara permanen.
pause
