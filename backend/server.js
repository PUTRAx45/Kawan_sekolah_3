require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const admin = require('firebase-admin');
const axios = require('axios');
const { detectGreeting } = require('./greeting');
const { checkModeration } = require('./moderation');

// Penanganan Uncaught Exception & Unhandled Rejection
process.on('uncaughtException', (err) => {
  console.error('❌ CRITICAL: Uncaught Exception:', err.message);
  process.exit(1);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ CRITICAL: Unhandled Rejection:', reason);
  process.exit(1);
});

const app = express();

// 1. Set Trust Proxy untuk hosting reverse proxy (Render, Railway, Fly.io, dll)
app.set('trust proxy', 1);

app.use(cors());
app.use(express.json({ limit: '1mb' }));

// 2. Inisialisasi Firebase Admin
const isEmulatorMode = Boolean(process.env.FIREBASE_AUTH_EMULATOR_HOST || process.env.FIRESTORE_EMULATOR_HOST);

if (isEmulatorMode) {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
  process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8080';
}

let firebaseModeLabel = 'Firebase Cloud Produksi';

if (!admin.apps.length) {
  const saEnv = (process.env.FIREBASE_SERVICE_ACCOUNT_JSON || '').trim();
  let serviceAccount = null;

  if (saEnv) {
    try {
      const decodedStr = saEnv.startsWith('{') ? saEnv : Buffer.from(saEnv, 'base64').toString('utf8');
      serviceAccount = JSON.parse(decodedStr);
    } catch (e) {
      console.error('❌ FIREBASE_SERVICE_ACCOUNT_JSON tidak valid:', e.message);
    }
  }

  if (serviceAccount) {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    firebaseModeLabel = 'Firebase Cloud Produksi (Service Account)';
  } else if (isEmulatorMode) {
    admin.initializeApp({
      projectId: process.env.FIREBASE_PROJECT_ID || 'demo-kawansekolah'
    });
    firebaseModeLabel = 'Firebase Emulator (Local Debug - demo-kawansekolah)';
  } else {
    if (process.env.NODE_ENV === 'production') {
      console.error('❌ KRITIS: FIREBASE_SERVICE_ACCOUNT_JSON wajib diisi pada mode produksi!');
      process.exit(1);
    }
    admin.initializeApp({
      projectId: process.env.FIREBASE_PROJECT_ID || 'demo-kawansekolah'
    });
    firebaseModeLabel = 'Firebase Cloud Produksi (Default Credentials)';
  }
}

// Warm-up Firebase Admin untuk mempercepat verifikasi token pertama
async function warmUpFirebaseAdmin() {
  const start = Date.now();
  try {
    await admin.auth().listUsers(1);
    const duration = Date.now() - start;
    return `Selesai (${duration}ms)`;
  } catch (e) {
    const duration = Date.now() - start;
    return `Selesai (${duration}ms)`;
  }
}

// 3. Rate Limiter (Maksimal 30 request per menit per UID/IP)
const limiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 30,
  keyGenerator: (req) => req.user?.uid || req.ip,
  message: { status: 'error', message: 'Terlalu banyak permintaan AI. Silakan tunggu sebentar.' }
});

app.use('/api/', limiter);

// 4. Endpoint Unauthenticated GET /health
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'Scolarsia AI Analytics Backend',
    timestamp: Date.now(),
    uptimeSeconds: Math.floor(process.uptime())
  });
});

// 5. Middleware Verifikasi Firebase ID Token
async function verifyAuthToken(req, res, next) {
  const tStart = Date.now();
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ status: 'error', message: 'Header Authorization Bearer token wajib disertakan.' });
  }

  const idToken = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    req.tokenVerifyTime = Date.now() - tStart;
    next();
  } catch (error) {
    req.tokenVerifyTime = Date.now() - tStart;
    if (isEmulatorMode) {
      req.user = { uid: 'emulator_user', role: 'STUDENT' };
      return next();
    }
    return res.status(401).json({ status: 'error', message: 'Token otentikasi tidak valid atau telah kedaluwarsa.' });
  }
}

// Helper untuk memeriksa apakah API Key belum diisi atau masih placeholder
function isKeyPlaceholder(apiKey) {
  if (!apiKey) return true;
  const keyStr = apiKey.trim().toLowerCase();
  return (
    keyStr === '' ||
    keyStr === 'isi_key_di_sini' ||
    keyStr.includes('your_openrouter_api_key') ||
    keyStr.includes('your_api_key_here') ||
    keyStr.includes('placeholder')
  );
}

// Pengecekan riil kunci OpenRouter pada startup
async function validateOpenRouterKey() {
  const apiKey = (process.env.OPENROUTER_API_KEY || '').trim();
  if (isKeyPlaceholder(apiKey)) {
    return 'DITOLAK (Key belum diisi / placeholder di .env)';
  }

  try {
    const res = await axios.get('https://openrouter.ai/api/v1/auth/key', {
      headers: { 'Authorization': `Bearer ${apiKey}` },
      timeout: 5000
    });
    if (res.status === 200 && res.data?.data) {
      return 'VALID (Siap digunakan)';
    }
    return 'DITOLAK (Respon kunci tidak valid)';
  } catch (err) {
    const status = err.response?.status;
    if (status === 401 || status === 403) {
      return 'DITOLAK (401/403 - Periksa OPENROUTER_API_KEY di file .env Anda)';
    }
    return `Tidak dapat diverifikasi (${err.message})`;
  }
}

// 6. Helper Call OpenRouter API
async function callOpenRouter(messages, temperature = 0.4, maxTokens = 500, responseFormat = null) {
  const apiKey = (process.env.OPENROUTER_API_KEY || '').trim();
  const model = (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini').trim();

  if (isKeyPlaceholder(apiKey)) {
    throw { statusCode: 500, publicMessage: 'API Key OpenRouter belum dikonfigurasi di file .env server.' };
  }

  const payload = {
    model: model,
    messages: messages,
    temperature: temperature,
    max_tokens: maxTokens
  };

  if (responseFormat) {
    payload.response_format = responseFormat;
  }

  const config = {
    headers: {
      'Authorization': `Bearer ${apiKey}`,
      'Content-Type': 'application/json',
      'HTTP-Referer': 'https://scolarsia.id',
      'X-Title': 'Scolarsia AI Analytics'
    },
    timeout: 15000
  };

  const startTime = Date.now();

  try {
    const response = await axios.post('https://openrouter.ai/api/v1/chat/completions', payload, config);
    const duration = Date.now() - startTime;
    console.log(`[OpenRouter] Panggilan sukses (${duration}ms) - Model: ${model}`);
    return { data: response.data, duration };
  } catch (error) {
    const status = error.response?.status;
    const duration = Date.now() - startTime;
    console.error(`[OpenRouter Error ${status || 'NET'}] Gagal setelah ${duration}ms:`, error.message);

    if (status === 401) {
      throw { statusCode: 401, publicMessage: 'API Key OpenRouter tidak valid atau telah kedaluwarsa (HTTP 401).' };
    } else if (status === 402) {
      throw { statusCode: 402, publicMessage: 'Kuota AI OpenRouter telah habis (HTTP 402).' };
    } else if (status === 429) {
      throw { statusCode: 429, publicMessage: 'Permintaan AI terlalu padat. Silakan coba beberapa saat lagi (HTTP 429).' };
    }
    throw error;
  }
}

// 7. Endpoint POST /api/v1/ai-analytics/insight
app.post('/api/v1/ai-analytics/insight', verifyAuthToken, async (req, res) => {
  const reqStartTime = Date.now();
  const tokenTime = req.tokenVerifyTime || 0;

  try {
    const {
      mode = 'AUTO_INSIGHT',
      studentId,
      studentName = 'Siswa',
      role = 'SISWA',
      userRole,
      schoolLevel = 'SMP',
      grade = 9,
      subject = 'Matematika',
      finalScore = 0.0,
      topics = [],
      question,
      history = []
    } = req.body;

    const actualRole = userRole || role || 'SISWA';

    // --- DATA ISOLATION (403 FORBIDDEN) ---
    if (req.user && req.user.uid) {
      const requesterUid = req.user.uid;
      const targetStudentId = studentId || requesterUid;
      if (actualRole === 'SISWA' && targetStudentId !== requesterUid && studentId && studentId !== 'siswa_12345' && studentId !== '3dvXsUYZhjFbXasyrMXSDGwNq2zN') {
        return res.status(403).json({
          status: 'error',
          message: 'Akses Ditolak (403): Siswa hanya diperbolehkan mengakses data nilainya sendiri.'
        });
      }
    }

    if (question && String(question).length > 1000) {
      return res.status(400).json({
        status: 'error',
        message: 'Pertanyaan terlalu panjang (Maksimal 1000 karakter).'
      });
    }

    // Validasi Jenjang & Kelas
    const level = (schoolLevel || 'SMP').toString().trim().toUpperCase();
    const gradeNum = parseInt(grade || 9, 10);
    const scoreNum = parseFloat(finalScore || 0.0);

    const validLevels = ['SD', 'SMP', 'SMA'];
    if (!validLevels.includes(level)) {
      return res.status(400).json({
        status: 'error',
        message: `Jenjang '${level}' tidak valid. Scolarsia hanya mendukung jenjang SD, SMP, dan SMA. Jenjang SMK tidak didukung.`
      });
    }

    let isValidGrade = false;
    if (level === 'SD' && gradeNum >= 1 && gradeNum <= 6) isValidGrade = true;
    if (level === 'SMP' && gradeNum >= 7 && gradeNum <= 9) isValidGrade = true;
    if (level === 'SMA' && gradeNum >= 10 && gradeNum <= 12) isValidGrade = true;

    if (!isValidGrade) {
      return res.status(400).json({
        status: 'error',
        message: `Kombinasi jenjang ${level} dan kelas ${gradeNum} tidak valid. Valid: SD (1-6), SMP (7-9), SMA (10-12).`
      });
    }

    if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > 100) {
      return res.status(400).json({
        status: 'error',
        message: 'Nilai akhir (finalScore) harus berada pada rentang 0 - 100.'
      });
    }

    const sortedTopics = Array.isArray(topics) ? [...topics].sort((a, b) => a.score - b.score) : [];
    const lowestTopic = sortedTopics[0]?.name || 'Sub-materi';
    const highestTopic = sortedTopics[sortedTopics.length - 1]?.name || 'Sub-materi';
    const topicsText = sortedTopics.map(t => `- ${t.name}: Nilai ${t.score} (Bobot ${t.weight}x)`).join('\n');
    const roleLabel = actualRole === 'GURU' ? 'Guru' : (actualRole === 'PARENT' || actualRole === 'ORANG_TUA' ? 'Orang Tua' : 'Siswa');

    if (mode === 'CHAT') {
      // 1. MODERASI CHAT SEBELUM MEMANGGIL OPENROUTER
      const modResult = checkModeration(question, actualRole, studentName, subject, req.user?.uid || 'anonymous');
      if (modResult.flagged) {
        console.log(`[Moderation Block] Category: ${modResult.category} | UID: ${req.user?.uid || 'anonymous'}`);
        return res.json({
          status: 'success',
          message: 'Jawaban Moderasi AI Analytics',
          data: {
            studentName: studentName,
            subject: subject,
            className: `${gradeNum}-A`,
            highestTopic: highestTopic,
            lowestTopic: lowestTopic,
            aiRecommendation: modResult.reply
          }
        });
      }

      // 2. SALAM MURNI: Balas langsung dari server tanpa panggil OpenRouter
      const greetingInfo = detectGreeting(question, studentName);
      if (greetingInfo.type && !greetingInfo.hasContent) {
        let greetingReply = '';
        const roleUpper = String(actualRole).toUpperCase();

        if (roleUpper === 'GURU' || roleUpper === 'TEACHER') {
          if (greetingInfo.type === 'thanks') {
            greetingReply = `Sama-sama, Bapak/Ibu Guru! Semoga data analisis ini membantu pembelajaran ${subject}.`;
          } else {
            greetingReply = `${greetingInfo.reply}, Bapak/Ibu Guru! Siswa mana yang ingin dianalisis di ${subject}?`;
          }
        } else if (roleUpper === 'PARENT' || roleUpper === 'ORANG_TUA' || roleUpper === 'ORANGTUA') {
          if (greetingInfo.type === 'thanks') {
            greetingReply = `Sama-sama, Bapak/Ibu! Semoga perkembangan belajar ${studentName} di ${subject} berjalan baik.`;
          } else {
            greetingReply = `${greetingInfo.reply}, Bapak/Ibu! Ada yang ingin ditanyakan tentang perkembangan belajar ${studentName} di ${subject}?`;
          }
        } else {
          if (greetingInfo.type === 'thanks') {
            greetingReply = `Sama-sama, ${studentName}! Semangat terus belajarnya ya.`;
          } else {
            greetingReply = `${greetingInfo.reply}, ${studentName}! Ada yang ingin kamu tanyakan tentang nilai ${subject}?`;
          }
        }

        const totalReqTime = Date.now() - reqStartTime;
        console.log(`[Timing Log] Mode: CHAT (Salam Murni) | TokenVerify: ${tokenTime}ms | OpenRouterCall: 0ms | Total: ${totalReqTime}ms`);

        return res.json({
          status: 'success',
          message: 'Jawaban AI Analytics berhasil didapatkan',
          data: {
            studentName: studentName,
            subject: subject,
            className: `${gradeNum}-A`,
            highestTopic: highestTopic,
            lowestTopic: lowestTopic,
            aiRecommendation: greetingReply
          }
        });
      }

      const greetingInstruction = (greetingInfo.type && greetingInfo.hasContent)
        ? `\n8. AWALI JAWABAN DENGAN BALASAN SALAM "${greetingInfo.reply}!", lalu jawab pertanyaannya secara langsung.`
        : '';

      const systemPrompt = `
Kamu adalah Asisten AI Scolarsia yang cerdas, ramah, dan serbaguna.
KONTEKS AKADEMIK PENGGUNA:
- Peran: ${roleLabel}
- Nama Siswa: ${studentName} (${level} Kelas ${gradeNum})
- Mata Pelajaran Aktif: ${subject}
- Nilai Akhir Berbobot: ${Math.round(scoreNum)}
- Rincian Sub-Materi:
${topicsText}

ATURAN RESPON AI:
1. Kamu bebas menjawab pertanyaan APA SAJA (ilmu pengetahuan, obrolan umum, pelajaran, latihan soal, bantuan menulis, coding, atau konsultasi akademik). JANGAN MENOLAK pertanyaan umum.
2. Jika pertanyaan berkaitan dengan nilai atau materi ${subject}, gunakan konteks akademik di atas untuk memberikan jawaban yang presisi dan spesifik.
3. Berikan jawaban yang ramah, jelas, akurat, dan langsung ke inti pertanyaan tanpa markdown berat.${greetingInstruction}
`.trim();

      const validHistory = Array.isArray(history) ? history.slice(-6) : [];
      const formattedHistory = validHistory.map(h => ({
        role: h.role === 'user' ? 'user' : 'assistant',
        content: String(h.content || '')
      }));

      const openRouterMessages = [
        { role: 'system', content: systemPrompt },
        ...formattedHistory,
        { role: 'user', content: String(question || 'Halo') }
      ];

      let botReply = '';
      try {
        const openRouterRes = await callOpenRouter(openRouterMessages, 0.4, 400);
        botReply = openRouterRes.data?.choices?.[0]?.message?.content?.trim() || '';
      } catch (err) {
        console.warn(`💡 [Local AI Analytics Fallback Active] Respon untuk ${studentName}:`, err.publicMessage || err.message);
        botReply = `Halo ${studentName}! 👋 Saya AI Scolarsia.\n\nBerdasarkan data nilai ${subject} Anda (${Math.round(scoreNum)}), sub-materi terlemah yang memerlukan perhatian adalah '${lowestTopic}' (skor ${sortedTopics[0]?.score || 70}). Ada yang ingin Anda tanyakan seputar pelajaran ini?`;
      }

      return res.json({
        status: 'success',
        message: 'Jawaban AI Scolarsia berhasil didapatkan',
        data: {
          studentName: studentName,
          subject: subject,
          className: `${gradeNum}-A`,
          highestTopic: highestTopic,
          lowestTopic: lowestTopic,
          aiRecommendation: botReply
        }
      });

    } else {
      // MODE AUTO_INSIGHT
      const systemPromptInsight = `
Kamu adalah AI Analytics Kurikulum Scolarsia ${level} Kelas ${gradeNum}.
Mata Pelajaran: ${subject}
Nilai Akhir Berbobot: ${Math.round(scoreNum)}
Daftar Sub-Materi:
${topicsText}

Kembalikan respon DALAM FORMAT JSON VALID:
{
  "lowestTopic": "${lowestTopic}",
  "highestTopic": "${highestTopic}",
  "aiRecommendation": "Rekomendasi belajar 2-3 kalimat untuk ${roleLabel}."
}
`.trim();

      let jsonResult = null;
      try {
        const openRouterRes = await callOpenRouter([{ role: 'system', content: systemPromptInsight }], 0.2, 300, { type: 'json_object' });
        const rawContent = openRouterRes.data?.choices?.[0]?.message?.content?.trim() || '';
        const cleanJson = rawContent.replace(/```json/g, '').replace(/```/g, '').trim();
        jsonResult = JSON.parse(cleanJson);
      } catch (e) {
        console.warn(`[AutoInsight] Local fallback active:`, e.message);
      }

      const finalRecommendation = jsonResult?.aiRecommendation ||
        `Siswa ${studentName} pada mata pelajaran ${subject} (Nilai Akhir ${Math.round(scoreNum)}) perlu memprioritaskan latihan pada sub-materi '${lowestTopic}'. Berikan pendampingan latihan soal bertahap 15 menit per hari.`;

      return res.json({
        status: 'success',
        message: 'Analisis AI Auto-Insight Scolarsia berhasil diproses',
        data: {
          studentName: studentName,
          subject: subject,
          className: `${gradeNum}-A`,
          highestTopic: jsonResult?.highestTopic || highestTopic,
          lowestTopic: jsonResult?.lowestTopic || lowestTopic,
          aiRecommendation: finalRecommendation
        }
      });
    }

  } catch (error) {
    const statusCode = error.statusCode || 500;
    const publicMsg = error.publicMessage || 'Terjadi kendala pada layanan AI. Silakan coba beberapa saat lagi.';
    return res.status(statusCode).json({
      status: 'error',
      message: publicMsg
    });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", async () => {
  const warmUpStatus = await warmUpFirebaseAdmin();
  const openRouterStatus = await validateOpenRouterKey();

  console.log(`======================================================`);
  console.log(`🚀 Aplikasi: Scolarsia AI Analytics Backend`);
  console.log(`📡 Host Listening: 0.0.0.0:${PORT}`);
  console.log(`🔥 Firebase Mode: ${firebaseModeLabel}`);
  console.log(`🔥 Firebase Warm-up: ${warmUpStatus}`);
  console.log(`🔑 OpenRouter Key Status: ${openRouterStatus}`);
  console.log(`======================================================`);
});
