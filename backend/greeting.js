/**
 * Modul Deteksi Salam Kustom (greeting.js) untuk Backend AI KawanSekolah.
 */

const FILLERS = [
  'kak', 'kaka', 'kakak', 'ai', 'min', 'admin', 'pak', 'bapak', 'bu', 'ibu',
  'bang', 'mas', 'mbak', 'tutor', 'asisten', 'semua', 'ya', 'yah', 'nih', 'dong',
  'bro', 'sis', 'dan', 'si'
];

function normalizeText(text) {
  if (!text) return '';
  return String(text)
    .toLowerCase()
    .replace(/[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/gu, '')
    .replace(/[^\w\s]/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function detectGreeting(rawText, studentName = '') {
  const norm = normalizeText(rawText);
  if (!norm) {
    return { type: null, reply: null, hasContent: false };
  }

  // Definisikan pola salam & balasan
  const greetingDefinitions = [
    // 1. Salam Waktu
    {
      type: 'time_malam',
      reply: 'Selamat malam',
      patterns: [
        'selamat malam', 'slamat malam', 'slmt malam', 'met malam',
        'selamat malem', 'slamat malem', 'slmt malem', 'met malem',
        'malam', 'malem'
      ]
    },
    {
      type: 'time_pagi',
      reply: 'Selamat pagi',
      patterns: [
        'selamat pagi', 'slamat pagi', 'slmt pagi', 'met pagi', 'pagi'
      ]
    },
    {
      type: 'time_siang',
      reply: 'Selamat siang',
      patterns: [
        'selamat siang', 'slamat siang', 'slmt siang', 'met siang', 'siang'
      ]
    },
    {
      type: 'time_sore',
      reply: 'Selamat sore',
      patterns: [
        'selamat sore', 'slamat sore', 'slmt sore', 'met sore', 'sore'
      ]
    },
    {
      type: 'time_petang',
      reply: 'Selamat petang',
      patterns: [
        'selamat petang', 'slamat petang', 'slmt petang', 'met petang', 'petang'
      ]
    },

    // 2. Salam Islami
    {
      type: 'islamic',
      reply: 'Waalaikumsalam',
      patterns: [
        'assalamualaikum', "assalamu'alaikum", 'assalamu alaikum',
        'assalamualaikum wr wb', "assalamu'alaikum wr wb", 'assalamu alaikum wr wb'
      ]
    },

    // 3. Salam Umum / Halo
    {
      type: 'general',
      reply: 'Halo',
      patterns: ['halo', 'hai', 'hi', 'hello', 'hey']
    },

    // 4. Ucapan Terima Kasih
    {
      type: 'thanks',
      reply: 'Sama-sama!',
      patterns: ['terima kasih', 'makasih', 'thanks', 'thx', 'trims', 'suwun']
    }
  ];

  let matchedType = null;
  let matchedReply = null;
  let matchedPattern = null;

  // Cari pattern terpanjang yang cocok terlebih dahulu
  for (const def of greetingDefinitions) {
    for (const pat of def.patterns) {
      const words = pat.split(' ');
      const normWords = norm.split(' ');

      // Cek apakah kalimat dimulai atau mengandung kata kunci salam ini
      if (norm === pat || norm.startsWith(pat + ' ') || norm.endsWith(' ' + pat) || norm.includes(' ' + pat + ' ')) {
        if (!matchedPattern || pat.length > matchedPattern.length) {
          matchedType = def.type;
          matchedReply = def.reply;
          matchedPattern = pat;
        }
      }
    }
  }

  // Jika tidak ditemukan salam
  if (!matchedType) {
    return {
      type: null,
      reply: null,
      hasContent: norm.length > 0
    };
  }

  // Evaluasi apakah ada konten/pertanyaan lain di luar salam dan kata sapaan pelengkap
  let remainingText = norm;
  if (matchedPattern) {
    remainingText = remainingText.replace(matchedPattern, ' ');
  }

  // Hapus kata-kata filler (kak, pak, bu, ai, min, dll.)
  const normStudentName = normalizeText(studentName);
  const studentWords = normStudentName ? normStudentName.split(' ') : [];

  const remainingWords = remainingText.split(' ').filter(word => {
    if (!word) return false;
    if (FILLERS.includes(word)) return false;
    if (studentWords.includes(word)) return false;
    // Hapus variasi wr wb
    if (word === 'wr' || word === 'wb' || word === 'wabarakatuh' || word === 'warahmatullahi') return false;
    return true;
  });

  const hasContent = remainingWords.length > 0;

  return {
    type: matchedType,
    reply: matchedReply,
    hasContent: hasContent
  };
}

module.exports = {
  detectGreeting,
  normalizeText
};
