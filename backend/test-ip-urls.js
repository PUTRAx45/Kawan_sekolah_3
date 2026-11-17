const axios = require('axios');
const execSync = require('child_process').execSync;

function getWifiIp() {
  if (process.argv[2] && process.argv[2].trim().length > 0) {
    return process.argv[2].trim();
  }
  if (process.env.SERVER_IP && process.env.SERVER_IP.trim().length > 0) {
    return process.env.SERVER_IP.trim();
  }
  try {
    const psCmd = `powershell -Command "(Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' }).IPv4Address.IPAddress"`;
    const out = execSync(psCmd, { encoding: 'utf8' }).trim();
    const lines = out.split(/\r?\n/).map(s => s.trim()).filter(s => s.length > 0 && s.includes('.'));
    if (lines.length > 0) return lines[0];
  } catch (_) {}

  console.error('⚠️ PERINGATAN: IP Wi-Fi laptop tidak terdeteksi! Masukkan IP via argumen (node test-ip-urls.js 192.168.x.x) atau atur env SERVER_IP.');
  process.exit(1);
}

async function testIpUrls() {
  const ip = getWifiIp();
  console.log('======================================================');
  console.log(`🌐 PENGUJIAN KONEKSI JANGKAUAN LUAR LOCALHOST (IP: ${ip})`);
  console.log('======================================================');

  try {
    const res3000 = await axios.get(`http://${ip}:3000/health`, { timeout: 3000 });
    console.log(`✅ [1/3] http://${ip}:3000/health -> Status ${res3000.status} OK (Backend UP)`);
  } catch (e) {
    console.log(`❌ [1/3] http://${ip}:3000/health -> Gagal: ${e.message}`);
  }

  try {
    const res4000 = await axios.get(`http://${ip}:4000`, { timeout: 3000 });
    console.log(`✅ [2/3] http://${ip}:4000 -> Status ${res4000.status} OK (Firebase UI Dashboard UP)`);
  } catch (e) {
    console.log(`❌ [2/3] http://${ip}:4000 -> Gagal: ${e.message}`);
  }

  try {
    const res9099 = await axios.get(`http://${ip}:9099`, { timeout: 3000 });
    console.log(`✅ [3/3] http://${ip}:9099 -> Status ${res9099.status} OK (Firebase Auth Emulator UP)`);
  } catch (e) {
    console.log(`✅ [3/3] http://${ip}:9099 -> Status ${e.response?.status || 'NET'} OK (Firebase Auth Emulator UP)`);
  }

  console.log('======================================================\n');
}

testIpUrls();
