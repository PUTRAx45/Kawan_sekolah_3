require('dotenv').config();
const axios = require('axios');

async function testKey() {
  console.log('======================================================');
  console.log('🔑 TESTING OPENROUTER API KEY CONNECTION');
  console.log('======================================================');

  const key = (process.env.OPENROUTER_API_KEY || '').trim();
  const model = (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini').trim();

  if (!key || key.includes('placeholder')) {
    console.error('❌ OPENROUTER_API_KEY belum diisi di backend/.env atau bernilai placeholder!');
    console.log('   Key terdeteksi:', key ? `${key.substring(0, 8)}...` : 'KOSONG');
    return;
  }

  console.log('📡 Mencoba menghubungi OpenRouter API dengan key:', `${key.substring(0, 8)}...`);
  console.log('🤖 Model:', model);

  try {
    const authCheck = await axios.get('https://openrouter.ai/api/v1/auth/key', {
      headers: { 'Authorization': `Bearer ${key}` },
      timeout: 10000
    });
    console.log('✅ Key Auth Check Result:', authCheck.data);
  } catch (e) {
    console.error('❌ Key Auth Check Failed:', e.response?.status, e.response?.data || e.message);
  }

  try {
    console.log('\n💬 Mencoba test chat completion...');
    const chatRes = await axios.post('https://openrouter.ai/api/v1/chat/completions', {
      model: model,
      messages: [{ role: 'user', content: 'Halo, katakan "OK"' }],
      max_tokens: 10
    }, {
      headers: {
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'https://scolarsia.id',
        'X-Title': 'Scolarsia AI Analytics'
      },
      timeout: 15000
    });

    console.log('✅ OpenRouter Chat Completion Response:');
    console.log('   Reply:', chatRes.data.choices?.[0]?.message?.content?.trim());
  } catch (e) {
    console.error('❌ Chat Completion Failed:', e.response?.status, e.response?.data || e.message);
  }

  console.log('======================================================\n');
}

testKey();
