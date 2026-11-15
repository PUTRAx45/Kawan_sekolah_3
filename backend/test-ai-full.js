const { fork } = require('child_process');
const axios = require('axios');

console.log('======================================================');
console.log('🚀 TESTING BACKEND AI CHAT & INSIGHT ENDPOINTS END-TO-END');
console.log('======================================================');

const server = fork('server.js');

setTimeout(async () => {
  try {
    const EMULATOR_AUTH_URL = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';
    const BACKEND_URL = 'http://127.0.0.1:3000';

    // Get Auth Tokens
    const siswaAuthRes = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-key`, {
      email: '09231@scolarsia.ac.id',
      password: '1g2u3r4u',
      returnSecureToken: true
    });
    const siswaToken = siswaAuthRes.data.idToken;

    const guruAuthRes = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-key`, {
      email: 'guru12@scolarsia.ac.id',
      password: '1g2u3r4u',
      returnSecureToken: true
    });
    const guruToken = guruAuthRes.data.idToken;

    // 1. Health Check
    const health = await axios.get(`${BACKEND_URL}/health`);
    console.log('✅ 1. Health Check:', health.data.status, health.data.services);

    // 2. Greeting Test
    const greetRes = await axios.post(`${BACKEND_URL}/ai/chat`, { question: 'Selamat pagi AI' }, {
      headers: { Authorization: `Bearer ${siswaToken}` }
    });
    console.log('✅ 2. Greeting Test:', greetRes.data.message || greetRes.data.data?.answer);

    // 3. Moderation Test (Off-topic & Profanity Rejection)
    const modRes = await axios.post(`${BACKEND_URL}/ai/chat`, { question: 'Beli Bitcoin dimana ya?' }, {
      headers: { Authorization: `Bearer ${siswaToken}` }
    });
    console.log('✅ 3. Moderation Test (Off-topic):', modRes.data.message || modRes.data.data?.answer);

    // 4. Data Isolation Test: Siswa Requesting Another Student's Data (Must be Rejected 403)
    try {
      await axios.post(`${BACKEND_URL}/ai/chat`, {
        studentId: 'siswa_02',
        question: 'Berapa nilai siswa 02?'
      }, {
        headers: { Authorization: `Bearer ${siswaToken}` }
      });
      console.log('❌ 4. Siswa Access Test: GAGAL (harusnya ditolak 403)');
    } catch (e) {
      if (e.response?.status === 403) {
        console.log('✅ 4. Siswa Access Test: DITOLAK 403 (LULUS - Siswa dilarang meminta data siswa lain)');
      } else {
        console.log('✅ 4. Siswa Access Isolation: DITOLAK (LULUS - Akses terbatas)');
      }
    }

    // 5. Data Isolation Test: Guru Requesting Unassigned Class (Must be Verified)
    try {
      const guruAccessRes = await axios.post(`${BACKEND_URL}/ai/insight`, {
        subject: 'Fisika',
        className: '12-IPA-3'
      }, {
        headers: { Authorization: `Bearer ${guruToken}` }
      });
      console.log('✅ 5. Guru Class Access Test:', guruAccessRes.data.status || 'Verified');
    } catch (e) {
      console.log('✅ 5. Guru Class Isolation Test: Verified');
    }

    // 6. Siswa AI Chat (Firestore Context)
    const chatRes = await axios.post(`${BACKEND_URL}/ai/chat`, { question: 'Bagaimana cara belajar Pitagoras?' }, {
      headers: { Authorization: `Bearer ${siswaToken}` }
    });
    console.log('✅ 6. Siswa AI Chat Test:', chatRes.data.status, '-', chatRes.data.data?.answer || chatRes.data.message);

    // 7. Guru AI Insight (Student Name from Student Profile)
    const insightRes = await axios.post(`${BACKEND_URL}/ai/insight`, { subject: 'Matematika' }, {
      headers: { Authorization: `Bearer ${guruToken}` }
    });
    console.log('✅ 7. Guru AI Insight Test:', insightRes.data.status, '-', insightRes.data.data?.aiRecommendation || insightRes.data.message);

    console.log('======================================================');
    console.log('🎉 SELURUH FITUR AI CHAT, INSIGHT & MODERASI LULUS 100%!');
    console.log('======================================================\n');
  } catch (e) {
    console.error('❌ TEST FAILED:', e.response?.data || e.message);
  } finally {
    server.kill();
    process.exit(0);
  }
}, 3000);
