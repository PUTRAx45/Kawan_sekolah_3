@echo off
chcp 65001 >nul
echo ======================================================
echo 🛡️ MEMBUKA WINDOWS FIREWALL UNTUK PORT SCOLARSIA
echo (Port 9099 Auth, 8081 Firestore, 8080 API, 3000 Backend)
echo ======================================================

:: Check Administrator Privileges
net session >nul 2>&1
if %errorLevel% NEQ 0 (
    echo ⚠️ Skrip ini memerlukan hak akses Administrator!
    echo 💡 Klik kanan file 'buka-firewall.bat' lalu pilih 'Run as administrator'.
    pause
    exit /b 1
)

echo.
echo 🔓 Menambahkan Aturan Inbound Firewall...

netsh advfirewall firewall delete rule name="Scolarsia Emulators & Backend" >nul 2>&1

netsh advfirewall firewall add rule name="Scolarsia Emulators & Backend" dir=in action=allow protocol=TCP localport=3000,8080,8081,9099,4000

echo.
echo ✅ Aturan Firewall berhasil ditambahkan! Port 3000, 8080, 8081, 9099, 4000 siap diakses dari HP Fisik.
pause
