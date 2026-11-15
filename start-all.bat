@echo off
chcp 65001 >nul
echo ======================================================
echo 🚀 STARTING SCOLARSIA EMULATOR & BACKEND SERVICES
echo ======================================================

echo.
echo 📦 [1/4] Menjalankan Firebase Emulator Suite di Docker (C:\firebase-docker)...
cd /d C:\firebase-docker
docker compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Gagal menjalankan Docker Compose. Pastikan Docker Desktop aktif.
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ⏳ [2/4] Menunggu Firebase Auth Emulator (port 9099) siap...
powershell -Command "$max=40; $count=0; while($count -lt $max){ try { $r=Invoke-WebRequest -Uri 'http://127.0.0.1:9099' -TimeoutSec 2 -ErrorAction Stop; if($r.StatusCode -eq 200){ Write-Host '✅ Auth Emulator Siap!'; break } } catch { $count++; Write-Host '. ($count/40)'; Start-Sleep -Seconds 2 } }; if($count -eq $max){ Write-Host '❌ Timed out waiting for Auth Emulator. Petunjuk: docker compose logs firebase-emulator --tail 40'; exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Gagal terhubung ke Firebase Auth Emulator.
    echo 💡 Petunjuk troubleshooting: Jalankan perintah 'docker compose logs firebase-emulator --tail 40' di C:\firebase-docker.
    echo.
    pause
    exit /b 1
)

echo.
echo 🧹 [3/4] Memeriksa dan mematikan proses lama di port 3000...
powershell -Command "Get-Process -Id (Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue).OwningProcess -ErrorAction SilentlyContinue | Stop-Process -Force"

echo.
echo 🌐 [4/4] INFORMASI ALAMAT IP WI-FI LAPTOP:
powershell -Command "Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' } | ForEach-Object { Write-Host '📌 IP Wi-Fi Laptop:' $_.IPv4Address.IPAddress }"

echo.
echo 🚀 Memulai Backend AI Node.js Express (port 3000)...
cd /d %~dp0backend
npm start
