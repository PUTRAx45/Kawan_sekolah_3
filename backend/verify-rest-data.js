process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';

const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: 'demo-kawansekolah'
  });
}

async function verifyRestData() {
  console.log('======================================================');
  console.log('🔍 VERIFIKASI AKUN AUTH & DOKUMEN FIRESTORE (REST/ADMIN)');
  console.log('======================================================');

  try {
    const listResult = await admin.auth().listUsers(100);
    const users = listResult.users;
    console.log(`\n📋 1. DAFTAR AKUN DI AUTH EMULATOR (TOTAL: ${users.length} AKUN):`);
    users.forEach((u, i) => {
      console.log(`   [${i + 1}] Email: ${u.email} | UID: ${u.uid} | Name: ${u.displayName}`);
    });

    const guruAuth = users.find(u => u.email === 'guru12@scolarsia.ac.id');
    if (guruAuth) {
      console.log(`\n📌 2. GURU AUTH ACCOUNT FOUND:`);
      console.log(`   UID Auth: ${guruAuth.uid}`);
      console.log(`   Email: ${guruAuth.email}`);

      const docPengguna = await admin.firestore().collection('pengguna').doc(guruAuth.uid).get();
      if (docPengguna.exists) {
        const data = docPengguna.data();
        console.log(`\n✅ [FIRESTORE] Dokumen 'pengguna/${guruAuth.uid}' DITEMUKAN!`);
        console.log(`   - ID Dokumen: ${guruAuth.uid} (SAMA PERSIS DENGAN AUTH UID)`);
        console.log(`   - nama / namaLengkap: "${data.namaLengkap || data.nama}"`);
        console.log(`   - email: "${data.email}"`);
        console.log(`   - role: "${data.role}"`);
        console.log(`   - aktif: ${data.aktif !== undefined ? data.aktif : data.isActive}`);
      } else {
        console.error(`❌ [FIRESTORE] Dokumen 'pengguna/${guruAuth.uid}' TIDAK DITEMUKAN!`);
      }

      const docUsers = await admin.firestore().collection('users').doc(guruAuth.uid).get();
      if (docUsers.exists) {
        const data = docUsers.data();
        console.log(`\n✅ [FIRESTORE] Dokumen 'users/${guruAuth.uid}' DITEMUKAN!`);
        console.log(`   - ID Dokumen: ${guruAuth.uid} (SAMA PERSIS DENGAN AUTH UID)`);
        console.log(`   - nama / namaLengkap: "${data.namaLengkap || data.nama}"`);
        console.log(`   - email: "${data.email}"`);
        console.log(`   - role: "${data.role}"`);
        console.log(`   - aktif: ${data.aktif !== undefined ? data.aktif : data.isActive}`);
      } else {
        console.error(`❌ [FIRESTORE] Dokumen 'users/${guruAuth.uid}' TIDAK DITEMUKAN!`);
      }
    }

  } catch (e) {
    console.error('❌ Verification Error:', e.message);
  }

  console.log('\n======================================================\n');
}

verifyRestData();
