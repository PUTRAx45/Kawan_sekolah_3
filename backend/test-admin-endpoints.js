const axios = require('axios');

async function testAdminEndpoints() {
  console.log('======================================================');
  console.log('🔍 TEST ENDPOINTS ADMIN (POST /admin/users, PUT, RESET, DELETE)');
  console.log('======================================================');

  try {
    // 1. Test POST /admin/users
    const createRes = await axios.post('http://127.0.0.1:3000/admin/users', {
      name: 'Guru Test Admin',
      email: 'gurutest_admin@scolarsia.ac.id',
      pass: '1g2u3r4u',
      role: 'guru',
      schoolLevel: 'SMP',
      grade: 9,
      className: '9-A'
    });
    console.log('✅ 1. POST /admin/users SUCCESS:', createRes.data.message);
    const createdUid = createRes.data.data.uid;

    // 2. Test PUT /admin/users/:uid (Toggle Aktif)
    const updateRes = await axios.put(`http://127.0.0.1:3000/admin/users/${createdUid}`, {
      aktif: false
    });
    console.log('✅ 2. PUT /admin/users/:uid SUCCESS:', updateRes.data.message);

    // 3. Test POST /admin/users/:uid/reset-password
    const resetRes = await axios.post(`http://127.0.0.1:3000/admin/users/${createdUid}/reset-password`, {
      newPassword: '1g2u3r4u_new'
    });
    console.log('✅ 3. POST /admin/users/:uid/reset-password SUCCESS:', resetRes.data.message);

    // 4. Test DELETE /admin/users/:uid
    const deleteRes = await axios.delete(`http://127.0.0.1:3000/admin/users/${createdUid}`);
    console.log('✅ 4. DELETE /admin/users/:uid SUCCESS:', deleteRes.data.message);

    console.log('======================================================');
    console.log('🎉 SELURUH ENDPOINT ADMIN TEST LULUS 100%!');
    console.log('======================================================\n');
  } catch (e) {
    console.error('❌ TEST FAILED:', e.response?.data || e.message);
  }
}

testAdminEndpoints();
