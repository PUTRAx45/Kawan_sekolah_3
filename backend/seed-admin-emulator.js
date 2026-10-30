const admin = require('firebase-admin');

process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8080';
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: process.env.FIREBASE_PROJECT_ID || 'demo-kawansekolah'
  });
}

const db = admin.firestore();

async function seedAdminEmulator() {
  console.log('======================================================');
  console.log('🌱 ADMIN SDK SEEDING (penugasan, users, scores)');
  console.log('======================================================\n');

  try {
    // 1. Seed Penugasan Collection
    const penugasanList = [
      { id: 'guru_smp_01_subj_matematika_9_cls_9a', teacherId: 'guru_smp_01', subjectId: 'subj_matematika_9', classId: 'cls_9a' },
      { id: 'guru_smp_03_subj_bindo_9_cls_9a', teacherId: 'guru_smp_03', subjectId: 'subj_bindo_9', classId: 'cls_9a' },
      { id: 'guru_smp_02_subj_ipa_9_cls_9b', teacherId: 'guru_smp_02', subjectId: 'subj_ipa_9', classId: 'cls_9b' }
    ];

    for (const p of penugasanList) {
      await db.collection('penugasan').doc(p.id).set(p);
      console.log(`✅ Penugasan seeded: ${p.id}`);
    }

    // 2. Seed Users Collection
    const usersList = [
      { uid: 'guru_smp_01', name: 'Budi Santoso', email: 'guru@kawansekolah.sch.id', role: 'GURU_MAPEL', teacherId: 'guru_smp_01', schoolLevel: 'SMP', grade: 9, className: '9-A' },
      { uid: 'guru_wali_01', name: 'Siti Aminah', email: 'guru.wali@kawansekolah.sch.id', role: 'WALI_KELAS', teacherId: 'guru_wali_01', classId: 'cls_9a', schoolLevel: 'SMP', grade: 9, className: '9-A' },
      { uid: 'std_1', name: 'Ahmad Budi', email: '12345@kawansekolah.local', role: 'SISWA', classId: 'cls_9a', schoolLevel: 'SMP', grade: 9, className: '9-A' },
      { uid: 'std_2', name: 'Siti Rahma', email: '12346@kawansekolah.local', role: 'SISWA', classId: 'cls_9a', schoolLevel: 'SMP', grade: 9, className: '9-A' }
    ];

    for (const u of usersList) {
      await db.collection('users').doc(u.uid).set(u);
      console.log(`✅ User seeded: ${u.uid} (${u.role})`);
    }

    // 3. Seed Scores Collection
    const scoresList = [
      { id: 'sc_1', studentId: 'std_1', studentName: 'Ahmad Budi', teacherId: 'guru_smp_01', subjectId: 'subj_matematika_9', classId: 'cls_9a', competenceId: 'komp_1', periodId: 'per_1', scoreType: 'KUIS_HARIAN', scoreValue: 85.0, comment: 'Bagus', timestamp: Date.now() },
      { id: 'sc_2', studentId: 'std_2', studentName: 'Siti Rahma', teacherId: 'guru_smp_01', subjectId: 'subj_matematika_9', classId: 'cls_9a', competenceId: 'komp_1', periodId: 'per_1', scoreType: 'KUIS_HARIAN', scoreValue: 92.0, comment: 'Sangat baik', timestamp: Date.now() },
      { id: 'sc_3', studentId: 'std_1', studentName: 'Ahmad Budi', teacherId: 'guru_smp_01', subjectId: 'subj_matematika_9', classId: 'cls_9a', competenceId: 'komp_2', periodId: 'per_1', scoreType: 'KARAKTER', scoreValue: 90.0, comment: 'Disiplin', timestamp: Date.now() }
    ];

    for (const s of scoresList) {
      await db.collection('scores').doc(s.id).set(s);
      console.log(`✅ Score seeded: ${s.id} (${s.scoreValue})`);
    }

    console.log('\n======================================================');
    console.log('✅ SEEDING ADMIN SDK BERHASIL SELESAI');
    console.log('======================================================');
  } catch (err) {
    console.error('❌ Error Admin SDK Seed:', err.message);
  }
}

seedAdminEmulator();
