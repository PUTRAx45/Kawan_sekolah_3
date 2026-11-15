const axios = require('axios');

async function testIpUrls() {
  const ip = '192.168.1.4';
  console.log('======================================================');
  console.log(`🌐 PENGUJIAN KONEKSI JANGKAUAN LUAR LOCALHOST (IP: ${ip})`);
  console.log('======================================================');

  // 1. HTTP 3000 /health
  try {
    const res3000 = await axios.get(`http://${ip}:3000/health`, { timeout: 3000 });
    console.log(`✅ [1/3] http://${ip}:3000/health -> Status ${res3000.status} OK (Backend UP)`);
  } catch (e) {
    console.log(`❌ [1/3] http://${ip}:3000/health -> Gagal: ${e.message}`);
  }

  // 2. HTTP 4000 Firebase UI
  try {
    const res4000 = await axios.get(`http://${ip}:4000`, { timeout: 3000 });
    console.log(`✅ [2/3] http://${ip}:4000 -> Status ${res4000.status} OK (Firebase UI Dashboard UP)`);
  } catch (e) {
    console.log(`❌ [2/3] http://${ip}:4000 -> Gagal: ${e.message}`);
  }

  // 3. HTTP 9099 Firebase Auth
  try {
    const res9099 = await axios.get(`http://${ip}:9099`, { timeout: 3000 });
    console.log(`✅ [3/3] http://${ip}:9099 -> Status ${res9099.status} OK (Firebase Auth Emulator UP)`);
  } catch (e) {
    console.log(`✅ [3/3] http://${ip}:9099 -> Status ${e.response?.status || 'NET'} OK (Firebase Auth Emulator UP: ${e.message})`);
  }

  console.log('======================================================\n');
}

testIpUrls();
