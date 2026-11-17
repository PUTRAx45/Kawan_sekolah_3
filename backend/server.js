require('dotenv').config();
const serverStartedAt = new Date().toISOString();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const admin = require('firebase-admin');
const axios = require('axios');
const { detectGreeting } = require('./greeting');
const { checkModeration } = require('./moderation');
const { seedData, clearDummyData } = require('./seed-manager');

// Penanganan Uncaught Exception & Unhandled Rejection
process.on('uncaughtException', (err) => {
  console.error('❌ CRITICAL: Uncaught Exception:', err.message);
  process.exit(1);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ CRITICAL: Unhandled Rejection:', reason);
  process.exit(1);
});

// 1. Ensure Emulator Default Env Vars for Non-Production
if (process.env.NODE_ENV !== 'production') {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
  process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';
  process.env.GCLOUD_PROJECT = process.env.GCLOUD_PROJECT || 'demo-kawansekolah';
}

const isEmulatorMode = Boolean(process.env.FIREBASE_AUTH_EMULATOR_HOST || process.env.FIRESTORE_EMULATOR_HOST);
const projectId = process.env.GCLOUD_PROJECT || 'demo-kawansekolah';

// Reject start if demo project ID is used without emulator active
if (projectId.startsWith('demo-') && !isEmulatorMode) {
  console.error(`\n❌ ERROR KRITIS: Project ID '${projectId}' adalah ID emulator lokal.`);
  console.error(`❌ Tidak dapat dijalankan pada mode Firebase Cloud Produksi tanpa emulator active!\n`);
  process.exit(1);
}

let firebaseModeLabel = isEmulatorMode
  ? `Firebase Emulator (Local Debug - ${projectId})`
  : 'Firebase Cloud Produksi';

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
  } else {
    admin.initializeApp({
      projectId: projectId
    });
  }
}

const app = express();

// Set Trust Proxy untuk hosting reverse proxy
app.set('trust proxy', 1);

app.use(cors());
app.use(express.json({ limit: '1mb' }));

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

// Rate Limiter
const limiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 30,
  keyGenerator: (req) => req.user?.uid || req.ip,
  message: { status: 'error', message: 'Terlalu banyak permintaan AI. Silakan tunggu sebentar.' }
});

app.use('/api/', limiter);

// Middleware Admin Verification
async function verifyAdminToken(req, res, next) {
  const authHeader = req.headers.authorization || '';
  if (!authHeader.startsWith('Bearer ')) {
    if (process.env.NODE_ENV !== 'production' || isEmulatorMode) {
      req.user = { uid: 'admin_01', role: 'admin' };
      return next();
    }
    return res.status(401).json({ status: 'error', message: 'Token otentikasi admin diperlukan.' });
  }

  const idToken = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    next();
  } catch (e) {
    if (process.env.NODE_ENV !== 'production' || isEmulatorMode) {
      req.user = { uid: 'admin_01', role: 'admin' };
      return next();
    }
    return res.status(401).json({ status: 'error', message: 'Token admin tidak valid: ' + e.message });
  }
}

// Middleware General Auth Verification
async function verifyAuthToken(req, res, next) {
  const authHeader = req.headers.authorization || '';
  if (!authHeader.startsWith('Bearer ')) {
    if (process.env.NODE_ENV !== 'production' || isEmulatorMode) {
      req.user = { uid: 'siswa_01', role: 'SISWA' };
      return next();
    }
    return res.status(401).json({ status: 'error', message: 'Token otentikasi diperlukan.' });
  }

  const idToken = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    next();
  } catch (e) {
    if (process.env.NODE_ENV !== 'production' || isEmulatorMode) {
      req.user = { uid: 'siswa_01', role: 'SISWA' };
      return next();
    }
    return res.status(401).json({ status: 'error', message: 'Token otentikasi tidak valid: ' + e.message });
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
  const apiKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
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

// Helper Call OpenRouter API / Hermes Agent
async function callOpenRouter(messages, temperature = 0.4, maxTokens = 500, responseFormat = null) {
  const apiKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
  const model = (process.env.OPENROUTER_MODEL || process.env.AI_MODEL || 'openai/gpt-4o-mini').trim();
  const baseUrl = (process.env.AI_BASE_URL || 'https://openrouter.ai/api/v1/chat/completions').trim();

  if (isKeyPlaceholder(apiKey)) {
    throw { statusCode: 502, code: 'AI_KEY_INVALID', publicMessage: 'AI belum dikonfigurasi atau kunci tidak valid' };
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
    timeout: 30000
  };

  const startTime = Date.now();

  try {
    const response = await axios.post(baseUrl, payload, config);
    const duration = Date.now() - startTime;
    console.log(`[OpenRouter/Hermes] Panggilan sukses (${duration}ms) - Model: ${model}`);
    return { data: response.data, duration };
  } catch (error) {
    const status = error.response?.status;
    const duration = Date.now() - startTime;
    console.error(`[OpenRouter Error ${status || 'NET'}] Gagal setelah ${duration}ms:`, error.message);

    if (status === 401 || status === 403) {
      throw { statusCode: 502, code: 'AI_KEY_INVALID', publicMessage: 'AI belum dikonfigurasi atau kunci tidak valid' };
    } else if (status === 402) {
      throw { statusCode: 502, code: 'AI_QUOTA', publicMessage: 'Kuota AI Analytics telah habis (HTTP 502). Silakan periksa akun OpenRouter Anda.' };
    } else if (status === 429) {
      throw { statusCode: 503, code: 'AI_RATE_LIMIT', publicMessage: 'Permintaan AI terlalu padat. Silakan tunggu sebentar dan coba lagi.' };
    }
    throw { statusCode: 503, code: 'AI_UNAVAILABLE', publicMessage: 'Layanan AI saat ini tidak dapat dijangkau. Silakan coba beberapa saat lagi.' };
  }
}

// Handler AI Analytics (CHAT & AUTO_INSIGHT)
const handleAiRequest = async (req, res) => {
  const reqStartTime = Date.now();
  const requesterUid = req.user?.uid || 'siswa_01';

  try {
    const { question, subject = 'Matematika' } = req.body;

    // 1. Check Greeting Detection First
    if (question) {
      const greetingReply = detectGreeting(question);
      if (greetingReply) {
        return res.json({
          status: 'success',
          modelCalled: false,
          message: 'Salam terdeteksi',
          data: {
            answer: greetingReply,
            aiRecommendation: greetingReply
          }
        });
      }

      // 2. Check Moderation Filter (Profanity, Off-topic, Privacy violation)
      const moderationResult = checkModeration(question);
      if (moderationResult && moderationResult.flagged) {
        return res.json({
          status: 'success',
          modelCalled: false,
          message: 'Pertanyaan memerlukan moderasi',
          data: {
            answer: moderationResult.reason || 'Maaf, mari tetap fokus membahas materi pelajaran, nilai, dan strategi belajar Anda.',
            aiRecommendation: moderationResult.reason || 'Mari tetap fokus membahas materi pelajaran dan strategi belajar.'
          }
        });
      }
    }

    // 3. Fetch User Profile & Role from Firestore directly
    let userDoc = null;
    try {
      const pDoc = await admin.firestore().collection('pengguna').doc(requesterUid).get();
      if (pDoc.exists) userDoc = pDoc.data();
      else {
        const uDoc = await admin.firestore().collection('users').doc(requesterUid).get();
        if (uDoc.exists) userDoc = uDoc.data();
      }
    } catch (_) {}

    const userRole = (userDoc?.role || 'siswa').toLowerCase();
    const studentName = userDoc?.namaLengkap || userDoc?.nama || 'Siswa';

    // 4. Access Rights Enforcement & Data Fetching from Firestore
    let targetStudentId = requesterUid;
    if (userRole === 'orang_tua' || userRole === 'parent') {
      const studentIds = userDoc?.studentIds || (userDoc?.studentId ? [userDoc.studentId] : []);
      if (studentIds.length > 0) {
        targetStudentId = studentIds[0];
      }
    }

    // Fetch Grades from Firestore (nilai or grades collection)
    let gradesSnapshot = null;
    try {
      if (userRole === 'guru' || userRole === 'teacher') {
        gradesSnapshot = await admin.firestore().collection('nilai').limit(10).get();
      } else {
        gradesSnapshot = await admin.firestore().collection('nilai').where('siswaUid', '==', targetStudentId).get();
        if (!gradesSnapshot || gradesSnapshot.empty) {
          gradesSnapshot = await admin.firestore().collection('grades').where('studentId', '==', targetStudentId).get();
        }
      }
    } catch (_) {}

    // 5. Check Grade Data Availability Requirement:
    if (!gradesSnapshot || gradesSnapshot.empty) {
      return res.json({
        status: 'success',
        modelCalled: false,
        message: 'Belum ada data nilai yang cukup untuk analisis AI',
        data: {
          answer: 'Belum ada data nilai yang cukup untuk analisis AI',
          aiRecommendation: 'Belum ada data nilai yang cukup untuk analisis AI'
        }
      });
    }

    // Extract grades for system prompt
    const gradesList = gradesSnapshot.docs.map(d => d.data());
    const validScores = gradesList.map(g => Number(g.nilaiAkhir || g.score || g.tugas || 0)).filter(s => s > 0);

    if (validScores.length === 0) {
      return res.json({
        status: 'success',
        modelCalled: false,
        message: 'Belum ada data nilai yang cukup untuk analisis AI',
        data: {
          answer: 'Belum ada data nilai yang cukup untuk analisis AI',
          aiRecommendation: 'Belum ada data nilai yang cukup untuk analisis AI'
        }
      });
    }

    const avgScore = Math.round(validScores.reduce((a, b) => a + b, 0) / validScores.length);

    // Call Hermes / OpenRouter AI model
    const systemPrompt = `
Kamu adalah Asisten AI Analytics Konsultan Pembelajaran Scolarsia (Agent Hermes).
Siswa: ${studentName} (${userRole.toUpperCase()})
Mata Pelajaran: ${subject}
Rata-rata Nilai: ${avgScore}

Berikan panduan belajar 2-3 kalimat yang membangun, ramah, dan aplikatif.
`.trim();

    const userMessage = question || `Berikan rekomendasi belajar terbaru untuk ${studentName} pada mapel ${subject}.`;

    try {
      const apiKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
      if (isKeyPlaceholder(apiKey)) {
        return res.status(502).json({
          status: 'error',
          modelCalled: false,
          code: 'AI_KEY_INVALID',
          message: 'AI belum dikonfigurasi atau kunci tidak valid'
        });
      }

      const openRouterRes = await callOpenRouter([
        { role: 'system', content: systemPrompt },
        { role: 'user', content: userMessage }
      ], 0.3, 300);

      const aiReply = openRouterRes.data?.choices?.[0]?.message?.content?.trim();
      if (!aiReply) {
        throw { statusCode: 502, code: 'AI_KEY_INVALID', publicMessage: 'AI belum dikonfigurasi atau kunci tidak valid' };
      }

      return res.json({
        status: 'success',
        modelCalled: true,
        message: 'Respons AI Scolarsia berhasil diproses',
        data: {
          answer: aiReply,
          aiRecommendation: aiReply,
          averageScore: avgScore
        }
      });
    } catch (aiErr) {
      const statusCode = aiErr.statusCode || 502;
      const code = aiErr.code || 'AI_KEY_INVALID';
      const publicMsg = aiErr.publicMessage || aiErr.message || 'AI belum dikonfigurasi atau kunci tidak valid';
      return res.status(statusCode).json({
        status: 'error',
        modelCalled: false,
        code: code,
        message: publicMsg
      });
    }
  } catch (error) {
    return res.status(500).json({
      status: 'error',
      code: 'SERVER_ERROR',
      message: error.message || 'Terjadi kendala pada server.'
    });
  }
};

app.post('/api/v1/ai-analytics/insight', verifyAuthToken, handleAiRequest);
app.post('/ai/chat', verifyAuthToken, (req, res) => { req.body.mode = 'CHAT'; return handleAiRequest(req, res); });
app.post('/ai/insight', verifyAuthToken, (req, res) => { req.body.mode = 'AUTO_INSIGHT'; return handleAiRequest(req, res); });

// Endpoints Admin (POST, PUT, RESET-PASSWORD, DELETE /admin/users)
app.post('/admin/users', verifyAdminToken, async (req, res) => {
  try {
    const { name, email, pass, role, schoolLevel, grade, className, studentIds } = req.body;

    if (!name || !email || !pass) {
      return res.status(400).json({ status: 'error', message: 'Nama, email, dan kata sandi wajib diisi.' });
    }

    let authUser = null;
    try {
      authUser = await admin.auth().createUser({
        email: email.trim(),
        password: pass,
        displayName: name.trim()
      });
    } catch (authErr) {
      if (authErr.code === 'auth/email-already-exists') {
        authUser = await admin.auth().getUserByEmail(email.trim());
      } else {
        throw authErr;
      }
    }

    const uid = authUser.uid;
    const normRole = (role || 'siswa').toLowerCase();

    try {
      await admin.auth().setCustomUserClaims(uid, { role: normRole });
    } catch (_) {}

    const userDocData = {
      uid: uid,
      sekolahId: 'sekolah_01',
      nama: name.trim(),
      namaLengkap: name.trim(),
      email: email.trim(),
      role: normRole,
      schoolLevel: schoolLevel || 'SMP',
      grade: grade || 9,
      className: className || '9-A',
      studentIds: studentIds || [],
      aktif: true,
      wajibGantiSandi: true,
      createdAt: Date.now()
    };

    await admin.firestore().collection('pengguna').doc(uid).set(userDocData, { merge: true });
    await admin.firestore().collection('users').doc(uid).set(userDocData, { merge: true });

    return res.json({
      status: 'success',
      message: `✅ Akun ${role || 'pengguna'} '${name}' berhasil dibuat!`,
      data: userDocData
    });
  } catch (error) {
    console.error('❌ Error /admin/users:', error);
    return res.status(500).json({ status: 'error', message: error.message || 'Gagal membuat akun.' });
  }
});

app.put('/admin/users/:uid', verifyAdminToken, async (req, res) => {
  try {
    const { uid } = req.params;
    const { name, role, aktif } = req.body;

    const updateData = { updatedAt: Date.now() };
    if (name !== undefined) updateData.nama = name;
    if (role !== undefined) updateData.role = role.toLowerCase();
    if (aktif !== undefined) updateData.aktif = Boolean(aktif);

    await admin.firestore().collection('pengguna').doc(uid).set(updateData, { merge: true });
    await admin.firestore().collection('users').doc(uid).set(updateData, { merge: true });

    return res.json({
      status: 'success',
      message: `✅ Akun ${uid} berhasil diperbarui!`
    });
  } catch (error) {
    return res.status(500).json({ status: 'error', message: error.message });
  }
});

app.post('/admin/users/:uid/reset-password', verifyAdminToken, async (req, res) => {
  try {
    const { uid } = req.params;
    const { newPassword } = req.body;
    const passToSet = newPassword || '1g2u3r4u';

    await admin.auth().updateUser(uid, { password: passToSet });

    await admin.firestore().collection('pengguna').doc(uid).set({ wajibGantiSandi: true }, { merge: true });
    await admin.firestore().collection('users').doc(uid).set({ wajibGantiSandi: true }, { merge: true });

    return res.json({
      status: 'success',
      message: `✅ Kata sandi akun ${uid} berhasil di-reset menjadi '${passToSet}'!`
    });
  } catch (error) {
    return res.status(500).json({ status: 'error', message: error.message });
  }
});

app.delete('/admin/users/:uid', verifyAdminToken, async (req, res) => {
  try {
    const { uid } = req.params;

    try {
      await admin.auth().deleteUser(uid);
    } catch (_) {}

    await admin.firestore().collection('pengguna').doc(uid).delete();
    await admin.firestore().collection('users').doc(uid).delete();

    return res.json({
      status: 'success',
      message: `✅ Akun ${uid} berhasil dihapus dari sistem.`
    });
  } catch (error) {
    return res.status(500).json({ status: 'error', message: error.message });
  }
});

// Endpoints Dev Seed (Active ONLY if NODE_ENV !== 'production')
app.post('/dev/seed', async (req, res) => {
  if (process.env.NODE_ENV === 'production') {
    return res.status(403).json({ status: 'error', message: 'Endpoint /dev/seed tidak tersedia di mode produksi.' });
  }

  try {
    await seedData();
    return res.json({ status: 'success', message: '✅ Data dummy berhasil diisi ke Auth & Firestore emulator!' });
  } catch (e) {
    console.error('❌ Error /dev/seed:', e);
    return res.status(500).json({ status: 'error', message: e.message || 'Gagal mengisi data dummy.' });
  }
});

app.post('/dev/seed-clear', async (req, res) => {
  if (process.env.NODE_ENV === 'production') {
    return res.status(403).json({ status: 'error', message: 'Endpoint /dev/seed-clear tidak tersedia di mode produksi.' });
  }

  try {
    await clearDummyData();
    return res.json({ status: 'success', message: '✅ Data dummy berhasil dibersihkan dari emulator!' });
  } catch (e) {
    console.error('❌ Error /dev/seed-clear:', e);
    return res.status(500).json({ status: 'error', message: e.message || 'Gagal menghapus data dummy.' });
  }
});

// 4. Endpoint Lightweight GET /health (Ultra-fast response for connection tests)
app.get('/health', (req, res) => {
  const pkg = require('./package.json');
  return res.json({
    status: 'UP',
    package: `${pkg.name}@${pkg.version}`,
    cwd: process.cwd(),
    timestamp: new Date().toISOString()
  });
});

// Detailed Health Check: GET /health/detail (2s timeout per emulator SDK check)
app.get('/health/detail', async (req, res) => {
  const checkAuthPromise = Promise.race([
    admin.auth().listUsers(1).then(() => 'OK').catch(e => `ERROR: ${e.message}`),
    new Promise(resolve => setTimeout(() => resolve('TIMEOUT (2s)'), 2000))
  ]);

  const checkFirestorePromise = Promise.race([
    admin.firestore().collection('health').doc('check').get().then(() => 'OK').catch(e => `ERROR: ${e.message}`),
    new Promise(resolve => setTimeout(() => resolve('TIMEOUT (2s)'), 2000))
  ]);

  const authStatus = await checkAuthPromise;
  const firestoreStatus = await checkFirestorePromise;

  const isHealthy = authStatus === 'OK' && firestoreStatus === 'OK';
  const pkg = require('./package.json');

  return res.status(isHealthy ? 200 : 503).json({
    status: isHealthy ? 'UP' : 'DEGRADED',
    package: `${pkg.name}@${pkg.version}`,
    cwd: process.cwd(),
    startedAt: serverStartedAt,
    firebaseMode: firebaseModeLabel,
    authHost: process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099',
    firestoreHost: process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081',
    timestamp: new Date().toISOString(),
    services: {
      backend: 'UP',
      firebaseAuthEmulator: authStatus,
      firestoreEmulator: firestoreStatus
    },
    environment: {
      port: PORT,
      projectId: process.env.GCLOUD_PROJECT || 'demo-kawansekolah',
      authHost: process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099',
      firestoreHost: process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081'
    }
  });
});

const PORT = process.env.PORT || 3000;
const server = app.listen(PORT, "0.0.0.0", async () => {
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

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`\n❌ ERROR: Port ${PORT} sudah digunakan oleh proses lain.`);
    console.error(`💡 Silakan jalankan 'stop-all.bat' atau hentikan proses di port ${PORT}.\n`);
    process.exit(1);
  } else {
    console.error('\n❌ Server Error:', err.message, '\n');
    process.exit(1);
  }
});
