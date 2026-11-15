@echo off
chcp 65001 >nul
echo ======================================================
echo 🌐 INFORMASI ALAMAT IP WI-FI LAPTOP SCOLARSIA
echo ======================================================

echo.
echo 📡 Mengambil IPv4 Wi-Fi laptop...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    set IP=%%a
    call :print_ip %%a
)

echo.
echo ======================================================
echo 💡 PETUNJUK PENGGUNAAN PADA HP FISIK:
echo 1. Pastikan HP dan laptop berada dalam satu Wi-Fi.
echo 2. Masukkan alamat IP laptop di atas pada dialog 'Alamat Server' di aplikasi.
echo ======================================================
pause
goto :eof

:print_ip
set CLEAN_IP=%*
echo 📌 IP Laptop Anda: %CLEAN_IP%
goto :eof
