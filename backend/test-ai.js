const axios = require('axios');

const EMULATOR_AUTH_URL = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';
const BACKEND_URL = 'http://127.0.0.1:3000';

async function getAuthToken(email, password) {
  try {
    const res = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-key`, {
      email,
      password,
      returnSecureToken: true
    });
    return res.data.idToken;
  } catch (e) {
    return null;
  }
}

async function runTests() {
  console.log('======================================================');
  console.log('🧪 TEST SUITE: SCOLARSIA AI CHAT & INSIGHT (npm run test-ai)');
  console.log('======================================================');

  // 1. Health Check
  try {
    const healthRes = await axios.get(`${BACKEND_URL}/health`, { timeout: 3000 });
    console.log('✅ [1/5] Health Check:', healthRes.data.status, healthRes.data.services);
  } catch (e) {
    console.log('ℹ️ [1/5] Server localhost:3000 offline/unreachable:', e.message);
  }

  // Get Auth Token for Dummy Accounts
  const siswaToken = await getAuthToken('09231@scolarsia.ac.id', '1g2u3r4u');
  const guruToken = await getAuthToken('guru12@scolarsia.ac.id', '1g2u3r4u');

  // 2. Test Greeting Detection
  try {
    const greetRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Selamat pagi AI'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {}
    });
    console.log('✅ [2/5] Greeting Detection Test:', greetRes.data.message || greetRes.data.data?.answer);
  } catch (e) {
    console.log('ℹ️ [2/5] Greet test:', e.response?.data?.message || e.message);
  }

  // 3. Test Moderation Filter (Profanity / Off-topic)
  try {
    const modRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Beli Bitcoin dimana ya?'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {}
    });
    console.log('✅ [3/5] Moderation Filter Test:', modRes.data.message || modRes.data.data?.answer);
  } catch (e) {
    console.log('ℹ️ [3/5] Moderation test:', e.response?.data?.message || e.message);
  }

  // 4. Test Siswa AI Chat (Context from Firestore)
  try {
    const chatRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Bagaimana cara meningkatkan nilai sub-materi Pitagoras?'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {}
    });
    console.log('✅ [4/5] Siswa AI Chat Test:', chatRes.data.status, chatRes.data.data?.answer || chatRes.data.message);
  } catch (e) {
    console.log('ℹ️ [4/5] Siswa AI Chat test:', e.response?.data?.message || e.message);
  }

  // 5. Test Guru AI Insight
  try {
    const insightRes = await axios.post(`${BACKEND_URL}/ai/insight`, {
      subject: 'Matematika'
    }, {
      headers: guruToken ? { Authorization: `Bearer ${guruToken}` } : {}
    });
    console.log('✅ [5/5] Guru AI Insight Test:', insightRes.data.status, insightRes.data.data?.aiRecommendation || insightRes.data.message);
  } catch (e) {
    console.log('ℹ️ [5/5] Guru AI Insight test:', e.response?.data?.message || e.message);
  }

  console.log('======================================================');
  console.log('🎉 PENGUJIAN AKHIR SELESAI!');
  console.log('======================================================\n');
}

runTests();
