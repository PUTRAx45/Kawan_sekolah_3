process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8080';

// Item 2: Pengecekan Keselamatan Emulator (Mencegah penulisan ke Firebase produksi asli)
if (!process.env.FIREBASE_AUTH_EMULATOR_HOST || !process.env.FIRESTORE_EMULATOR_HOST) {
  console.error('\n❌ ERROR KESELAMATAN SEED:');
  console.error('   FIREBASE_AUTH_EMULATOR_HOST dan FIRESTORE_EMULATOR_HOST wajib terdefinisi!');
  console.error('   Skrip dihentikan untuk mencegah penulisan ke Firebase produksi asli.\n');
  process.exit(1);
}

const admin = require('firebase-admin');
const axios = require('axios');

const PROJECT_ID = 'kawansekolahai'; // Item 1: Diselaraskan dengan google-services.json

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: PROJECT_ID
  });
}

const auth = admin.auth();
const db = admin.firestore();

async function createOrUpdateUser(uid, email, password, displayName) {
  try {
    await auth.getUser(uid);
    await auth.updateUser(uid, { email, password, displayName });
    console.log(`[Seed Auth] User ${uid} (${email}) berhasil diperbarui.`);
  } catch (err) {
    if (err.code === 'auth/user-not-found') {
      await auth.createUser({ uid, email, password, displayName });
      console.log(`[Seed Auth] User ${uid} (${email}) berhasil dibuat.`);
    } else {
      throw err;
    }
  }
}

async function verifyAuthRest(email, password, label) {
  const url = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-key';
  try {
    const res = await axios.post(url, {
      email: email,
      password: password,
      returnSecureToken: true
    });
    console.log(`✅ [REST Auth Verified] ${label} (${email}) -> UID: ${res.data.localId}`);
    return true;
  } catch (e) {
    console.error(`❌ [REST Auth Failed] ${label} (${email}) -> Error:`, e.response?.data || e.message);
    return false;
  }
}

async function runSeed() {
  console.log('======================================================');
  console.log(`🌱 MEMULAI SEED DATA EMULATOR FIREBASE (${PROJECT_ID})`);
  console.log('   Host Auth: ' + process.env.FIREBASE_AUTH_EMULATOR_HOST);
  console.log('   Host Firestore: ' + process.env.FIRESTORE_EMULATOR_HOST);
  console.log('======================================================\n');

  try {
    // 1. Akun Guru
    const teacherUid = 'guru_smp_01';
    const teacherEmail = 'guru@kawansekolah.sch.id';
    await createOrUpdateUser(teacherUid, teacherEmail, '123456', 'Drs. Budi Raharjo');

    await db.collection('users').doc(teacherUid).set({
      uid: teacherUid,
      name: 'Drs. Budi Raharjo',
      email: teacherEmail,
      role: 'TEACHER',
      teacherId: teacherUid
    });

    await db.collection('teaching_profiles').doc(teacherUid).set({
      teacherId: teacherUid,
      fullName: 'Drs. Budi Raharjo',
      schoolName: 'SMP Negeri 1 Jakarta',
      schoolLevel: 'SMP',
      classesTaught: ['9-A', '9-B'],
      profileComplete: true
    });

    // 2. Akun Siswa (NIS: 12345)
    const studentUid = 'siswa_12345';
    const studentEmail = '12345@kawansekolah.local';
    await createOrUpdateUser(studentUid, studentEmail, '123456', 'Ahmad Budi');

    await db.collection('users').doc(studentUid).set({
      uid: studentUid,
      name: 'Ahmad Budi',
      email: studentEmail,
      role: 'STUDENT',
      teacherId: teacherUid,
      schoolLevel: 'SMP',
      grade: 9,
      className: '9-A'
    });

    // 3. Akun Orang Tua (Item 4: Domain lokal kawansekolah.local)
    const parentUid = 'orangtua_01';
    const parentEmail = 'ortu.ahmad@kawansekolah.local';
    await createOrUpdateUser(parentUid, parentEmail, '123456', 'Siti Rahma');

    await db.collection('users').doc(parentUid).set({
      uid: parentUid,
      name: 'Siti Rahma (Orang Tua Ahmad)',
      email: parentEmail,
      role: 'PARENT',
      teacherId: teacherUid,
      studentIds: [studentUid]
    });

    // 4. Mapel & Topics
    const mathSubjectId = 'subj_matematika_9';
    await db.collection('subjects').doc(mathSubjectId).set({
      id: mathSubjectId,
      teacherId: teacherUid,
      name: 'Matematika',
      schoolLevel: 'SMP',
      grade: 9
    });

    await db.collection('topics').doc('top_aritmatika').set({
      id: 'top_aritmatika',
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      name: 'Aritmatika',
      weight: 1.0
    });

    await db.collection('topics').doc('top_geometri').set({
      id: 'top_geometri',
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      name: 'Geometri',
      weight: 2.0
    });

    await db.collection('topics').doc('top_pitagoras').set({
      id: 'top_pitagoras',
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      name: 'Pitagoras',
      weight: 1.5
    });

    // 5. Sampel Nilai
    await db.collection('grades').doc(`${studentUid}_top_aritmatika`).set({
      id: `${studentUid}_top_aritmatika`,
      studentId: studentUid,
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      subjectName: 'Matematika',
      topicId: 'top_aritmatika',
      topicName: 'Aritmatika',
      score: 90.0,
      createdAt: Date.now()
    });

    await db.collection('grades').doc(`${studentUid}_top_geometri`).set({
      id: `${studentUid}_top_geometri`,
      studentId: studentUid,
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      subjectName: 'Matematika',
      topicId: 'top_geometri',
      topicName: 'Geometri',
      score: 90.0,
      createdAt: Date.now()
    });

    await db.collection('grades').doc(`${studentUid}_top_pitagoras`).set({
      id: `${studentUid}_top_pitagoras`,
      studentId: studentUid,
      teacherId: teacherUid,
      subjectId: mathSubjectId,
      subjectName: 'Matematika',
      topicId: 'top_pitagoras',
      topicName: 'Pitagoras',
      score: 70.0,
      createdAt: Date.now()
    });

    console.log('\n======================================================');
    console.log('🔍 MEMVERIFIKASI TIGA AKUN DENGAN REST AUTH EMULATOR');
    console.log('======================================================\n');

    const v1 = await verifyAuthRest(teacherEmail, '123456', 'Akun Guru');
    const v2 = await verifyAuthRest(studentEmail, '123456', 'Akun Siswa (NIS 12345)');
    const v3 = await verifyAuthRest(parentEmail, '123456', 'Akun Orang Tua');

    console.log('\n======================================================');
    if (v1 && v2 && v3) {
      console.log('🎉 SEED DATA & VERIFIKASI 100% SUKSES!');
    } else {
      console.warn('⚠️ SEED DATA BERHASIL, TETAPI SEBAGIAN VERIFIKASI AUTH GAGAL.');
    }
    console.log('======================================================\n');

  } catch (err) {
    console.error('❌ Gagal menjalankan Seed Emulator:', err);
  }
}

runSeed();
