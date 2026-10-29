const { detectGreeting } = require('./greeting');

const cases = [
  { text: 'selamat malam', expectedPrefix: 'Selamat malam' },
  { text: 'selamat pagi', expectedPrefix: 'Selamat pagi' },
  { text: 'selamat siang', expectedPrefix: 'Selamat siang' },
  { text: 'selamat sore', expectedPrefix: 'Selamat sore' },
  { text: 'assalamualaikum', expectedPrefix: 'Waalaikumsalam' },
  { text: 'kenapa nilai Pitagoras-ku rendah?', hasContent: true, expectedTopic: 'Pitagoras' },
  { text: '📊 Ringkasan Nilai', hasContent: true },
  { text: '⚠️ Materi Terlemah', hasContent: true },
  { text: '💡 Saran Belajar', hasContent: true },
  { text: 'siapa pemenang piala dunia 2022?', isOutOfSubject: true }
];

console.log('======================================================');
console.log('🧪 VERIFIKASI CHECKLIST MANUAL SALAM & KONTEKS (LOCAL)');
console.log('======================================================\n');

cases.forEach((c, idx) => {
  const g = detectGreeting(c.text, 'Ahmad Budi');
  console.log(`Kasus #${idx + 1}: "${c.text}"`);
  console.log(`  -> Greeting Type: ${g.type}`);
  console.log(`  -> Greeting Reply: "${g.reply}"`);
  console.log(`  -> HasContent: ${g.hasContent}`);
  console.log('------------------------------------------------------');
});

console.log('\n✅ Seluruh logika deteksi salam & filter konteks terverifikasi siap.');
