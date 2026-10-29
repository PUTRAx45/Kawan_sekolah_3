const axios = require('axios');

const BASE_URL = 'http://localhost:3000/api/v1/ai-analytics/insight';

const baseData = {
  studentId: 'siswa_12345',
  studentName: 'Ahmad Budi',
  role: 'SISWA',
  schoolLevel: 'SMP',
  grade: 9,
  subject: 'Matematika',
  finalScore: 83.3,
  topics: [
    { name: 'Aritmatika', score: 90.0, weight: 1.0 },
    { name: 'Geometri', score: 90.0, weight: 2.0 },
    { name: 'Pitagoras', score: 70.0, weight: 1.5 }
  ]
};

const samplePayloadGreeting = {
  ...baseData,
  mode: 'CHAT',
  question: 'selamat malam'
};

const samplePayloadGreetingPagi = {
  ...baseData,
  mode: 'CHAT',
  question: 'selamat pagi'
};

const samplePayloadGreetingSoreWithQuestion = {
  ...baseData,
  mode: 'CHAT',
  question: 'selamat sore, apa sub-materi yang paling lemah?'
};

const samplePayloadAnalysis = {
  ...baseData,
  mode: 'CHAT',
  question: 'kenapa nilai Pitagoras-ku rendah dan harus belajar apa?',
  history: [
    { role: 'user', content: 'selamat malam' },
    { role: 'assistant', content: 'Selamat malam Ahmad Budi! Ada yang bisa saya bantu terkait nilai Matematika kamu?' }
  ]
};

async function runTests() {
  console.log('============== MEMULAI UJI PENGIRIMAN BAGIAN A ==============');

  // Test 1: Sapaan Murni Malam
  console.log('\n--- UJI 1: Sapaan Murni ("selamat malam") ---');
  try {
    const res1 = await axios.post(BASE_URL, samplePayloadGreeting, {
      headers: { 'Authorization': 'Bearer mock-token-test' }
    });
    console.log('Status Response:', res1.data.status);
    console.log('AI Recommendation:', res1.data.data?.aiRecommendation);
  } catch (err) {
    console.error('Uji 1 Gagal/Error:', err.response?.data || err.message);
  }

  // Test 2: Sapaan Murni Pagi
  console.log('\n--- UJI 2: Sapaan Murni ("selamat pagi") ---');
  try {
    const res2 = await axios.post(BASE_URL, samplePayloadGreetingPagi, {
      headers: { 'Authorization': 'Bearer mock-token-test' }
    });
    console.log('Status Response:', res2.data.status);
    console.log('AI Recommendation:', res2.data.data?.aiRecommendation);
  } catch (err) {
    console.error('Uji 2 Gagal/Error:', err.response?.data || err.message);
  }

  // Test 3: Sapaan Sore Dengan Pertanyaan
  console.log('\n--- UJI 3: Sapaan Sore Dengan Pertanyaan ("selamat sore, apa sub-materi yang paling lemah?") ---');
  try {
    const res3 = await axios.post(BASE_URL, samplePayloadGreetingSoreWithQuestion, {
      headers: { 'Authorization': 'Bearer mock-token-test' }
    });
    console.log('Status Response:', res3.data.status);
    console.log('AI Recommendation:', res3.data.data?.aiRecommendation);
  } catch (err) {
    console.error('Uji 3 Gagal/Error:', err.response?.data || err.message);
  }

  // Test 4: Analisis Nilai
  console.log('\n--- UJI 4: Pertanyaan Analisis Nilai Pitagoras ---');
  try {
    const res4 = await axios.post(BASE_URL, samplePayloadAnalysis, {
      headers: { 'Authorization': 'Bearer mock-token-test' }
    });
    console.log('Status Response:', res4.data.status);
    console.log('AI Recommendation:', res4.data.data?.aiRecommendation);
  } catch (err) {
    console.error('Uji 4 Gagal/Error:', err.response?.data || err.message);
  }

  console.log('\n============================================================');
}

runTests();
