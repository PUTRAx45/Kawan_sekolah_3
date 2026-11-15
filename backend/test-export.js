const axios = require('axios');

async function triggerExport() {
  console.log('📦 Triggering manual Firebase Emulator Export to /data...');
  try {
    const res = await axios.post('http://127.0.0.1:4000/emulator/v1/export', {
      path: '/data'
    });
    console.log('✅ Export Response:', res.status, res.data);
  } catch (e) {
    console.error('❌ Export Failed:', e.response?.data || e.message);
  }
}

triggerExport();
