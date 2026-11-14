@echo off
chcp 65001 >nul
echo ======================================================
echo 🛑 STOPPING SCOLARSIA EMULATOR & BACKEND SERVICES
echo ======================================================

echo.
echo 🧹 [1/2] Menghentikan Backend Node.js di port 3000...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000 ^| findstr LISTENING 2^>nul') do (
    echo 🛑 Menutup proses Backend PID: %%a...
    taskkill /f /pid %%a >nul 2>&1
)

echo.
echo 📦 [2/2] Menghentikan Firebase Emulator & Meng-export Data ke ./emulator-data...
cd /d C:\firebase-docker
docker compose down

echo.
echo ✅ Semua layanan berhasil dihentikan dan data emulator tersimpan.
pause
