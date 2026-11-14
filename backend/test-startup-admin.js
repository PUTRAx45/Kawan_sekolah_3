const { fork } = require('child_process');
const axios = require('axios');

console.log('======================================================');
console.log('🚀 TESTING BACKEND ADMIN ENDPOINTS END-TO-END');
console.log('======================================================');

const server = fork('server.js');

setTimeout(async () => {
  try {
    // 1. POST /admin/users
    const res = await axios.post('http://127.0.0.1:3000/admin/users', {
      name: 'Guru Test Admin',
      email: 'gurutest_admin@scolarsia.ac.id',
      pass: '1g2u3r4u',
      role: 'guru',
      schoolLevel: 'SMP',
      grade: 9,
      className: '9-A'
    });
    console.log('✅ 1. POST /admin/users:', res.data.message);
    const uid = res.data.data.uid;

    // 2. PUT /admin/users/:uid
    const putRes = await axios.put(`http://127.0.0.1:3000/admin/users/${uid}`, {
      aktif: false
    });
    console.log('✅ 2. PUT /admin/users/:uid:', putRes.data.message);

    // 3. POST /admin/users/:uid/reset-password
    const resetRes = await axios.post(`http://127.0.0.1:3000/admin/users/${uid}/reset-password`, {
      newPassword: '1g2u3r4u_new'
    });
    console.log('✅ 3. POST /admin/users/:uid/reset-password:', resetRes.data.message);

    // 4. DELETE /admin/users/:uid
    const delRes = await axios.delete(`http://127.0.0.1:3000/admin/users/${uid}`);
    console.log('✅ 4. DELETE /admin/users/:uid:', delRes.data.message);

    console.log('======================================================');
    console.log('🎉 SELURUH ENDPOINT ADMIN TEST LULUS 100%!');
    console.log('======================================================\n');
  } catch (e) {
    console.error('❌ FAIL:', e.response?.data || e.message);
  } finally {
    server.kill();
    process.exit(0);
  }
}, 3000);
