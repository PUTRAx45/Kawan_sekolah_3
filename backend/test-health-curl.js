const { fork } = require('child_process');
const axios = require('axios');

console.log('======================================================');
console.log('📡 TESTING GET /health ENDPOINT (HTTP 200)');
console.log('======================================================');

const server = fork('server.js');

setTimeout(async () => {
  try {
    const res = await axios.get('http://127.0.0.1:3000/health');
    console.log('✅ GET /health Status:', res.status);
    console.log('📊 JSON Payload:\n', JSON.stringify(res.data, null, 2));
    console.log('======================================================');
    console.log('🎉 GET /health TEST LULUS 100%!');
    console.log('======================================================\n');
  } catch (e) {
    console.error('❌ FAIL:', e.response?.data || e.message);
  } finally {
    server.kill();
    process.exit(0);
  }
}, 2500);
