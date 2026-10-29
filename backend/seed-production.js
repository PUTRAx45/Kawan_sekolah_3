const readline = require('readline');
const admin = require('firebase-admin');

// Keamanan: Hanya berjalan jika diberi flag --production
const args = process.argv.slice(2);
if (!args.includes('--production')) {
  console.error('\n❌ PERINGATAN KEAMANAN: Skrip seed produksi ini hanya dapat dijalankan dengan flag --production!');
  console.error('   Penggunaan: node seed-production.js --production\n');
  process.exit(1);
}

// Pastikan tidak berjalan pada environment emulator
if (process.env.FIREBASE_AUTH_EMULATOR_HOST || process.env.FIRESTORE_EMULATOR_HOST) {
  console.error('\n❌ GAGAL: Skrip seed produksi menolak berjalan saat variabel emulator terdeteksi!');
  process.exit(1);
}

const saEnv = (process.env.FIREBASE_SERVICE_ACCOUNT_JSON || '').trim();
let serviceAccount = null;

if (saEnv) {
  try {
    const decodedStr = saEnv.startsWith('{') ? saEnv : Buffer.from(saEnv, 'base64').toString('utf8');
    serviceAccount = JSON.parse(decodedStr);
  } catch (e) {
    console.error('❌ FIREBASE_SERVICE_ACCOUNT_JSON tidak valid.');
  }
}

if (!serviceAccount) {
  console.error('\n❌ GAGAL: Variabel lingkungan FIREBASE_SERVICE_ACCOUNT_JSON wajib diisi!');
  process.exit(1);
}

const projectId = serviceAccount.project_id || process.env.FIREBASE_PROJECT_ID;

if (!projectId) {
  console.error('\n❌ GAGAL: Project ID tidak dapat ditemukan dari Service Account.');
  process.exit(1);
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

console.log(`\n================================================================`);
console.log(`⚠️ KONFIRMASI INI AKAN MEMBUAT AKUN GURU UTAMA DI FIREBASE PRODUKSI!`);
console.log(`   Project ID sasaran: ${projectId}`);
console.log(`================================================================\n`);

rl.question(`Ketik ulang nama Project ID ('${projectId}') untuk mengonfirmasi: `, async (answer) => {
  if (answer.trim() !== projectId) {
    console.error('\n❌ Konfirmasi nama Project ID tidak cocok. Skrip dibatalkan.\n');
    rl.close();
    process.exit(1);
  }

  try {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });

    const teacherEmail = 'guru.utama@scolarsia.sch.id';
    const teacherPassword = 'ScolarsiaGuru2025!';
    const teacherName = 'Drs. Budi Raharjo';

    console.log(`\n🔄 Membuat akun Guru Utama di Firebase Auth (${teacherEmail})...`);

    let userRecord;
    try {
      userRecord = await admin.auth().getUserByEmail(teacherEmail);
      console.log(`ℹ️ Akun Auth sudah ada dengan UID: ${userRecord.uid}`);
    } catch (e) {
      userRecord = await admin.auth().createUser({
        email: teacherEmail,
        password: teacherPassword,
        displayName: teacherName
      });
      console.log(`✅ Akun Auth Guru berhasil dibuat dengan UID: ${userRecord.uid}`);
    }

    const db = admin.firestore();
    const teacherUid = userRecord.uid;

    await db.collection('users').doc(teacherUid).set({
      uid: teacherUid,
      name: teacherName,
      email: teacherEmail,
      role: 'TEACHER',
      teacherId: teacherUid,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    await db.collection('teaching_profiles').doc(teacherUid).set({
      teacherId: teacherUid,
      fullName: teacherName,
      schoolName: 'SMP Negeri 1 Jakarta',
      schoolLevel: 'SMP',
      classesTaught: ['9-A', '9-B'],
      profileComplete: true
    }, { merge: true });

    console.log(`✅ Dokumen profil Guru di Firestore berhasil dibuat/diperbarui!`);
    console.log(`\n================================================================`);
    console.log(`🎉 SEED PRODUKSI SELESAI!`);
    console.log(`   Email Guru: ${teacherEmail}`);
    console.log(`   Password:   ${teacherPassword}`);
    console.log(`================================================================\n`);

  } catch (err) {
    console.error('\n❌ Terjadi kesalahan saat seed produksi:', err.message);
  } finally {
    rl.close();
    process.exit(0);
  }
});
