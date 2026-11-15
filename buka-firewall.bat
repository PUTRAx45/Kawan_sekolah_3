@echo off
chcp 65001 >nul
echo ======================================================
echo 🛡️ MEMBUKA WINDOWS FIREWALL UNTUK PORT SCOLARSIA
echo (Port 3000 Backend API, 8081 Firestore, 9099 Auth, 4000 UI)
echo ======================================================

net session >nul 2>&1
if %errorLevel% NEQ 0 (
    echo.
    echo ⚠️ HAK AKSES ADMINISTRATOR DIPERLUKAN!
    echo 💡 Silakan klik kanan file 'buka-firewall.bat' lalu pilih 'Run as administrator'.
    echo.
    pause
    exit /b 1
)

echo.
echo 🔓 Menghapus aturan lama dan menambahkan aturan Inbound TCP (Port 3000, 8081, 9099, 4000)...
netsh advfirewall firewall delete rule name="Scolarsia Emulators & Backend" >nul 2>&1
netsh advfirewall firewall add rule name="Scolarsia Emulators & Backend" dir=in action=allow protocol=TCP localport=3000,8081,9099,4000 profile=any

echo.
echo ✅ Aturan Firewall berhasil ditambahkan dengan sukses!
echo 🌐 Port 3000, 8081, 9099, 4000 siap diakses dari HP Fisik pada jaringan Wi-Fi.
echo.
pause
