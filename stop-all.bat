@echo off
chcp 65001 >nul
echo ======================================================
echo 🛑 STOPPING SCOLARSIA EMULATOR DAN BACKEND SERVICES
echo ======================================================

echo.
echo 🧹 [1/2] Memeriksa dan menghentikan Backend Node.js di port 3000...
powershell -Command "$conn = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue; if ($conn) { Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host '🛑 Proses Backend port 3000 dihentikan.' } else { Write-Host '✅ Port 3000 bebas.' }"

echo.
echo 📦 [2/2] Menghentikan Firebase Emulator via docker compose stop...
cd /d C:\firebase-docker
docker compose stop

echo.
echo ⏳ Memeriksa berkas hasil export di ./emulator-data/export...
dir /b C:\firebase-docker\emulator-data\export 2>nul

echo.
echo ✅ Semua layanan berhasil dihentikan dan data emulator tersimpan secara permanen.
pause
