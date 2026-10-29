const { detectGreeting } = require('./greeting');

const testCases = [
  { input: 'selamat pagi', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat pagi' },
  { input: 'selamat siang', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat siang' },
  { input: 'selamat sore', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat sore' },
  { input: 'selamat malam', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat malam' },
  { input: 'Selamat Malam!!', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat malam' },
  { input: 'slamat malem kak', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat malam' },
  { input: 'halo', expectedType: true, expectedHasContent: false, expectedReply: 'Halo' },
  { input: 'assalamualaikum wr wb', expectedType: true, expectedHasContent: false, expectedReply: 'Waalaikumsalam' },
  { input: 'terima kasih', expectedType: true, expectedHasContent: false, expectedReply: 'Sama-sama!' },
  { input: 'selamat malam, nilai pitagoras saya kenapa?', expectedType: true, expectedHasContent: true, expectedReply: 'Selamat malam' },
  { input: 'kenapa nilai pitagoras rendah?', expectedType: false, expectedHasContent: true, expectedReply: null },
  { input: 'malam', expectedType: true, expectedHasContent: false, expectedReply: 'Selamat malam' }
];

function runGreetingTests() {
  console.log('======================================================');
  console.log('🧪 PENGUJIAN DETEKSI SALAM (detectGreeting)');
  console.log('======================================================\n');

  let passed = 0;
  let failed = 0;

  testCases.forEach((tc, index) => {
    const res = detectGreeting(tc.input, 'Ahmad Budi');
    const isTypeOk = tc.expectedType ? (res.type !== null) : (res.type === null);
    const isHasContentOk = (res.hasContent === tc.expectedHasContent);
    const isReplyOk = tc.expectedReply === null ? (res.reply === null) : (res.reply === tc.expectedReply);

    const isSuccess = isTypeOk && isHasContentOk && isReplyOk;

    if (isSuccess) {
      passed++;
      console.log(`[LULUS] Kasus #${index + 1}: "${tc.input}" -> Reply: "${res.reply}", HasContent: ${res.hasContent}`);
    } else {
      failed++;
      console.error(`[GAGAL] Kasus #${index + 1}: "${tc.input}"`);
      console.error(`        Hasil: Reply="${res.reply}", HasContent=${res.hasContent}, Type=${res.type}`);
      console.error(`        Ekspektasi: Reply="${tc.expectedReply}", HasContent=${tc.expectedHasContent}, IsGreeting=${tc.expectedType}`);
    }
  });

  console.log('\n======================================================');
  console.log(`RINGKASAN HASIL: ${passed} LULUS, ${failed} GAGAL`);
  console.log('======================================================');

  if (failed > 0) {
    process.exit(1);
  }
}

runGreetingTests();
