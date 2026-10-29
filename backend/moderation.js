const badwords = require('./badwords.json');

// Memory tracker untuk pelanggaran bahasa kasar per UID (3x dalam 10 menit -> mute 5 menit)
const userViolations = new Map(); // uid -> { count: number, resetAt: number, mutedUntil: number }

function normalizeForModeration(text) {
  if (!text) return '';

  let norm = String(text)
    .toLowerCase()
    // Strip control characters
    .replace(/[\x00-\x1F\x7F-\x9F]/g, '')
    // Subtitusi angka dan simbol mirip huruf
    .replace(/4/g, 'a')
    .replace(/@/g, 'a')
    .replace(/1/g, 'i')
    .replace(/!/g, 'i')
    .replace(/0/g, 'o')
    .replace(/3/g, 'e')
    .replace(/\$/g, 's')
    // Hapus spasi dan titik antar karakter tunggal (m.e.m.e.k -> memek)
    .replace(/(\b\w)\s+(\b\w)/g, '$1$2')
    .replace(/(\b\w)\.+(\b\w)/g, '$1$2')
    // Mampatkan karakter berulang (anjiiing -> anjing)
    .replace(/(.)\1+/g, '$1')
    .trim();

  return norm;
}

function checkModeration(text, role = 'SISWA', studentName = 'Siswa', subject = 'Matematika', uid = 'anonymous') {
  if (!text) {
    return { flagged: false };
  }

  // 1. Batas Panjang Pesan (Maksimal 500 Karakter)
  if (text.length > 500) {
    return {
      flagged: true,
      category: 'length_limit',
      reply: 'Mohon maaf, pertanyaanmu terlalu panjang (maksimal 500 karakter). Silakan persingkat pertanyaanmu.'
    };
  }

  const rawLower = text.toLowerCase();
  const norm = normalizeForModeration(text);

  // 2. Mute Tracker Check
  const now = Date.now();
  const vData = userViolations.get(uid) || { count: 0, resetAt: now + 10 * 60 * 1000, mutedUntil: 0 };

  if (vData.mutedUntil > now) {
    const remainingMins = Math.ceil((vData.mutedUntil - now) / (60 * 1000));
    return {
      flagged: true,
      category: 'muted',
      reply: `Fitur chat untuk akunmu sementara dinonaktifkan selama ${remainingMins} menit karena penggunaan bahasa tidak sopan berulang.`
    };
  }

  // 3. Bahasa Kasar / Menghina
  const isBadWord = badwords.some(bw => {
    const normBw = normalizeForModeration(bw);
    return rawLower.includes(bw) || norm.includes(normBw);
  });

  if (isBadWord) {
    if (now > vData.resetAt) {
      vData.count = 1;
      vData.resetAt = now + 10 * 60 * 1000;
    } else {
      vData.count += 1;
    }

    if (vData.count >= 3) {
      vData.mutedUntil = now + 5 * 60 * 1000; // Mute 5 menit
      userViolations.set(uid, vData);
      console.warn(`[Moderation Mute] UID ${uid} di-mute selama 5 menit karena 3x pelanggaran bahasa kasar.`);
      return {
        flagged: true,
        category: 'profanity_mute',
        reply: 'Mari gunakan bahasa yang sopan ya. Fitur chat dihentikan sementara selama 5 menit.'
      };
    }

    userViolations.set(uid, vData);
    console.warn(`[Moderation Profanity] UID ${uid} terdeteksi bahasa kasar (Pelanggaran ke-${vData.count}).`);
    return {
      flagged: true,
      category: 'profanity',
      reply: 'Mari gunakan bahasa yang sopan ya. Aku siap membantu soal nilai dan pelajaran.'
    };
  }

  // 4. Meminta Informasi Pribadi Orang Lain (PII Request)
  const piiKeywords = [
    'nomor hp', 'no hp', 'nomor telepon', 'no wa', 'nomor whatsapp', 'alamat rumah',
    'password guru', 'password siswa', 'nilai siswa lain', 'alamat guru', 'akun siswa lain'
  ];
  if (piiKeywords.some(kw => rawLower.includes(kw))) {
    return {
      flagged: true,
      category: 'pii_request',
      reply: 'Mohon maaf, AI Analytics hanya membahas data nilai dan perkembangan belajar milikmu sendiri (atau anak untuk orang tua).'
    };
  }

  // 5. Menanyakan Identitas AI / System Prompt / Jailbreak
  const jailbreakKeywords = [
    'kamu model apa', 'model llm apa', 'tampilkan system prompt', 'system prompt',
    'ignore previous instructions', 'abaikan instruksi sebelumnya', 'siapa pembuatmu', 'api key'
  ];
  if (jailbreakKeywords.some(kw => rawLower.includes(kw))) {
    return {
      flagged: true,
      category: 'ai_identity_leak',
      reply: 'Aku adalah Asisten AI Analytics KawanSekolah. Aku siap membantumu menganalisis nilai dan memberikan rekomendasi belajar.'
    };
  }

  // 6. Jelas Di Luar Pelajaran & Nilai (Off-Topic Khusus)
  const offTopicStrict = [
    'piala dunia', 'juara liga', 'presiden indonesia', 'resep makanan',
    'film bioskop', 'sepatu roda', 'ramalan zodiak'
  ];
  if (offTopicStrict.some(kw => rawLower.includes(kw))) {
    return {
      flagged: true,
      category: 'off_topic',
      reply: `Aku adalah asisten khusus belajar KawanSekolah. Yuk, tanyakan hal seputar nilai dan materi pelajaran ${subject}!`
    };
  }

  return { flagged: false };
}

module.exports = {
  checkModeration,
  normalizeForModeration
};
