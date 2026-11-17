@echo off
chcp 65001 >nul
echo ======================================================
echo 🚀 STARTING SCOLARSIA EMULATOR DAN BACKEND SERVICES
echo ======================================================

set FIREBASE_AUTH_EMULATOR_HOST=127.0.0.1:9099
set FIRESTORE_EMULATOR_HOST=127.0.0.1:8081
set GCLOUD_PROJECT=demo-kawansekolah

echo.
echo 📦 [1/6] Menjalankan Firebase Emulator Suite di Docker (C:\firebase-docker)...
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
echo ⏳ [2/6] Menunggu Firebase Auth Emulator (port 9099) siap...
powershell -Command "$max=40; $count=0; while($count -lt $max){ try { $r=Invoke-WebRequest -Uri 'http://127.0.0.1:9099' -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop; if($r.StatusCode -eq 200){ Write-Host '✅ Auth Emulator Siap!'; break } } catch { $count++; Write-Host (\". (\" + $count + \"/40)\"); Start-Sleep -Seconds 2 } }; if($count -eq $max){ Write-Host '❌ Timed out waiting for Auth Emulator.'; exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Gagal terhubung ke Firebase Auth Emulator.
    echo 💡 Petunjuk troubleshooting: Jalankan 'docker compose logs firebase-emulator --tail 40' di C:\firebase-docker.
    echo.
    pause
    exit /b 1
)

echo.
echo ⏳ [3/6] Menunggu Cloud Firestore Emulator (port 8081) siap...
powershell -Command "$max=40; $count=0; while($count -lt $max){ try { $r=Invoke-WebRequest -Uri 'http://127.0.0.1:8081' -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop; if($r.Content -match 'Ok' -or $r.StatusCode -eq 200){ Write-Host '✅ Firestore Emulator Siap!'; break } } catch { $count++; Write-Host (\". (\" + $count + \"/40)\"); Start-Sleep -Seconds 2 } }; if($count -eq $max){ Write-Host '❌ Timed out waiting for Firestore Emulator.'; exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Gagal terhubung ke Cloud Firestore Emulator di port 8081.
    echo.
    pause
    exit /b 1
)

echo.
echo 🔍 [4/6] Memeriksa kelengkapan data akun di Auth dan Firestore...
cd /d %~dp0backend
for /f "usebackq tokens=1,2 delims=," %%a in (`node -e "const admin=require('firebase-admin'); process.env.FIREBASE_AUTH_EMULATOR_HOST='127.0.0.1:9099'; process.env.FIRESTORE_EMULATOR_HOST='127.0.0.1:8081'; if(!admin.apps.length) admin.initializeApp({projectId:'demo-kawansekolah'}); Promise.all([admin.auth().listUsers(1000), admin.firestore().collection('users').get()]).then(([u, d]) => console.log(u.users.length + ',' + d.size)).catch(() => console.log('0,0'));" 2^>nul`) do (
    set AUTH_COUNT=%%a
    set DOC_COUNT=%%b
)

if "%AUTH_COUNT%"=="" set AUTH_COUNT=0
if "%DOC_COUNT%"=="" set DOC_COUNT=0

set NEED_SEED=0
if %AUTH_COUNT% LSS 15 set NEED_SEED=1
if %DOC_COUNT% LSS 15 set NEED_SEED=1

if %NEED_SEED%==1 goto doseed
if %NEED_SEED%==0 goto skipseed

:doseed
echo.
echo ⚠️ Data akun kurang (Auth: %AUTH_COUNT%, Dokumen users: %DOC_COUNT%). Mengisi data dummy via 'call npm run seed'...
call npm run seed
if errorlevel 1 (
    echo.
    echo ⚠️ SEED GAGAL, backend tetap dijalankan tetapi login mungkin gagal.
    timeout /t 3 >nul
)
goto continue_start

:skipseed
echo.
echo ✅ Data akun sudah ada (%AUTH_COUNT% akun Auth dan %DOC_COUNT% dokumen users). Melewati proses seed.
goto continue_start

:continue_start
echo.
echo 🧹 [5/6] Memeriksa dan mematikan proses lama di port 3000...
powershell -Command "$conn = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue; if ($conn) { Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host '🛑 Proses lama di port 3000 dihentikan.' } else { Write-Host '✅ Port 3000 bebas.' }"

echo.
echo 🌐 [6/6] INFORMASI ALAMAT IP WI-FI LAPTOP SCOLARSIA:
for /f "usebackq tokens=*" %%i in (`powershell -Command "(Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' }).IPv4Address.IPAddress" 2^>nul`) do set WIFI_IP=%%i

if "%WIFI_IP%"=="" set WIFI_IP=192.168.1.4

echo.
echo ======================================================
echo 📡 ALAMAT IP UNTUK APLIKASI DI HP FISIK: %WIFI_IP%
echo ======================================================

echo.
echo 🔬 Menguji keterjangkauan 3 port via IP Wi-Fi (%WIFI_IP%)...
powershell -Command "$ip='%WIFI_IP%'; $failCount=0; foreach($p in @(3000, 9099, 8081)){ try { $s=New-Object System.Net.Sockets.TcpClient; $s.Connect($ip, $p); $s.Close(); Write-Host ('   • Port ' + $p + ': ✅ OK') } catch { $failCount++; Write-Host ('   • Port ' + $p + ': ❌ GAGAL (Port ditutup / Firewall / Offline)') } }; if($failCount -eq 3){ Write-Host '⚠️ IP laptop kemungkinan berubah!' }"

echo.
echo 🚀 Memulai Health Check Async dan Backend AI Node.js Express (port 3000)...
start /b powershell -Command "Start-Sleep -Seconds 3; try { $h=Invoke-RestMethod -Uri 'http://127.0.0.1:3000/health' -UseBasicParsing -TimeoutSec 3; Write-Host '✅ Health Check Backend:' ($h | ConvertTo-Json -Compress) } catch { Write-Host '⚠️ Backend startup in progress...' }"
cd /d %~dp0backend
call npm start
