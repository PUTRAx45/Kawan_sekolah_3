@echo off
chcp 65001 >nul
echo ======================================================
echo 🌐 INFORMASI ALAMAT IP WI-FI LAPTOP SCOLARSIA
echo ======================================================

echo.
echo 📡 Mengambil IPv4 dan MAC Address Wi-Fi laptop...
powershell -Command "Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' } | ForEach-Object { Write-Host '📌 IP Wi-Fi Laptop :' $_.IPv4Address.IPAddress; Write-Host '🔒 MAC Address     :' $_.NetAdapter.MacAddress; Write-Host '📡 Nama Adapter    :' $_.NetAdapter.InterfaceDescription }"

echo.
echo ======================================================
echo 💡 PETUNJUK PENGGUNAAN PADA HP FISIK:
echo 1. Pastikan HP dan laptop berada dalam satu jaringan Wi-Fi.
echo 2. Masukkan alamat IP laptop di atas pada dialog 'Alamat Server' (ikon ⚙️ di layar login).
echo 3. Untuk mencegah IP berubah tiap kali terkoneksi ke router Wi-Fi,
echo    gunakan MAC Address di atas untuk mendaftarkan 'DHCP IP Reservation'
echo    pada pengaturan router Wi-Fi Anda.
echo 4. Tes koneksi dari browser HP dengan membuka URL:
echo    http://IP_LAPTOP:3000/health
echo ======================================================
echo.
pause
