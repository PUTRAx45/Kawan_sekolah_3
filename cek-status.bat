@echo off
chcp 65001 >nul
echo ======================================================
echo 📊 CHECKING SCOLARSIA EMULATOR & BACKEND STATUS
echo ======================================================

echo.
echo 📦 [STATUS DOCKER CONTAINER]
cd /d C:\firebase-docker
docker compose ps

echo.
echo 📡 [STATUS HEALTH CHECK BACKEND API (http://localhost:3000/health)]
powershell -Command "try { (Invoke-RestMethod -Uri 'http://localhost:3000/health') | ConvertTo-Json -Depth 5 } catch { Write-Host '❌ Backend offline atau tidak dapat diakses.' }"

echo.
pause
