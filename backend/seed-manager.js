process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';

const admin = require('firebase-admin');

const PROJECT_ID = process.env.GCLOUD_PROJECT || 'demo-kawansekolah';

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: PROJECT_ID
  });
}

const auth = admin.auth();
const db = admin.firestore();

const DUMMY_PASS = '1g2u3r4u';

const DUMMY_USERS = [
  { uid: 'admin_01', email: 'admin12@scolarsia.ac.id', name: 'Arif Setiawan, S.Kom.', role: 'admin' },
  { uid: 'guru_01', email: 'guru12@scolarsia.ac.id', name: 'Budi Santoso, S.Pd.', role: 'guru', classes: ['9-A', '9-B'] },
  { uid: 'wali_01', email: 'walikelas12@scolarsia.ac.id', name: 'Siti Aminah, M.Pd.', role: 'wali_kelas', classes: ['9-A'] },

  // 6 Siswa Kelas 9-A
  { uid: 'siswa_01', email: '09231@scolarsia.ac.id', name: 'Ahmad Budi', role: 'siswa', nis: '09231', class: '9-A' },
  { uid: 'siswa_02', email: '09232@scolarsia.ac.id', name: 'Siti Rahma', role: 'siswa', nis: '09232', class: '9-A' },
  { uid: 'siswa_03', email: '09233@scolarsia.ac.id', name: 'Dewi Lestari', role: 'siswa', nis: '09233', class: '9-A' },
  { uid: 'siswa_04', email: '09234@scolarsia.ac.id', name: 'Fajar Pratama', role: 'siswa', nis: '09234', class: '9-A' },
  { uid: 'siswa_05', email: '09235@scolarsia.ac.id', name: 'Bima Saputra', role: 'siswa', nis: '09235', class: '9-A' },
  { uid: 'siswa_06', email: '09236@scolarsia.ac.id', name: 'Aulia Putri', role: 'siswa', nis: '09236', class: '9-A' },

  // 6 Orang Tua
  { uid: 'ortu_01', email: 'ortu_09231@scolarsia.ac.id', name: 'Bapak Ahmad', role: 'orang_tua', studentId: 'siswa_01' },
  { uid: 'ortu_02', email: 'ortu_09232@scolarsia.ac.id', name: 'Ibu Rahma', role: 'orang_tua', studentId: 'siswa_02' },
  { uid: 'ortu_03', email: 'ortu_09233@scolarsia.ac.id', name: 'Bapak Lestari', role: 'orang_tua', studentId: 'siswa_03' },
  { uid: 'ortu_04', email: 'ortu_09234@scolarsia.ac.id', name: 'Bapak Pratama', role: 'orang_tua', studentId: 'siswa_04' },
  { uid: 'ortu_05', email: 'ortu_09235@scolarsia.ac.id', name: 'Ibu Saputra', role: 'orang_tua', studentId: 'siswa_05' },
  { uid: 'ortu_06', email: 'ortu_09236@scolarsia.ac.id', name: 'Ibu Putri', role: 'orang_tua', studentId: 'siswa_06' }
];

async function seedData() {
  console.log(`🌱 [SeedManager] Membuat akun Auth & data Firestore dummy (Project: ${PROJECT_ID})...`);

  // 1. Seed Auth Users
  for (const u of DUMMY_USERS) {
    try {
      try {
        await auth.getUser(u.uid);
        await auth.updateUser(u.uid, {
          email: u.email,
          password: DUMMY_PASS,
          displayName: u.name
        });
      } catch (err) {
        if (err.code === 'auth/user-not-found') {
          await auth.createUser({
            uid: u.uid,
            email: u.email,
            password: DUMMY_PASS,
            displayName: u.name
          });
        }
      }

      await auth.setCustomUserClaims(u.uid, { isDummy: true, role: u.role });
    } catch (e) {
      console.warn(`⚠️ [Seed Auth] Warning user ${u.email}:`, e.message);
    }
  }

  // 2. Seed Firestore Collections
  const batch = db.batch();

  // Koleksi: pengguna & users
  for (const u of DUMMY_USERS) {
    const userDocData = {
      uid: u.uid,
      sekolahId: 'sekolah_01',
      nama: u.name,
      namaLengkap: u.name,
      role: u.role,
      email: u.email,
      nis: u.nis || '',
      noHp: '08123456789',
      aktif: true,
      wajibGantiSandi: false,
      namaSekolah: 'SMP Scolarsia 1',
      jenjang: 'SMP',
      kelasDiampu: u.classes || [],
      studentIds: u.studentId ? [u.studentId] : [],
      isDummy: true,
      createdAt: Date.now()
    };

    batch.set(db.collection('pengguna').doc(u.uid), userDocData, { merge: true });
    batch.set(db.collection('users').doc(u.uid), userDocData, { merge: true });
  }

  // Sekolah & Tahun Ajaran & Periode
  batch.set(db.collection('sekolah').doc('sekolah_01'), { id: 'sekolah_01', nama: 'SMP Scolarsia 1', isDummy: true });
  batch.set(db.collection('tahunAjaran').doc('ta_2026'), { id: 'ta_2026', sekolahId: 'sekolah_01', nama: '2026/2027', aktif: true, isDummy: true });
  batch.set(db.collection('periode').doc('periode_ganjil'), {
    id: 'periode_ganjil',
    sekolahId: 'sekolah_01',
    nama: 'Semester Ganjil 2026/2027',
    aktif: true,
    bobotTugas: 30,
    bobotUts: 30,
    bobotUas: 40,
    isDummy: true
  });

  // Kelas & Mapel & SubMateri
  batch.set(db.collection('kelas').doc('kelas_9a'), { id: 'kelas_9a', sekolahId: 'sekolah_01', jenjang: 'SMP', nama: '9-A', waliKelasUid: 'wali_01', isDummy: true });
  batch.set(db.collection('kelas').doc('kelas_9b'), { id: 'kelas_9b', sekolahId: 'sekolah_01', jenjang: 'SMP', nama: '9-B', waliKelasUid: '', isDummy: true });

  batch.set(db.collection('mapel').doc('mapel_matematika'), { id: 'mapel_matematika', sekolahId: 'sekolah_01', nama: 'Matematika', jenjang: 'SMP', isDummy: true });
  batch.set(db.collection('mapel').doc('mapel_ipa'), { id: 'mapel_ipa', sekolahId: 'sekolah_01', nama: 'IPA', jenjang: 'SMP', isDummy: true });

  batch.set(db.collection('subMateri').doc('sub_aritmatika'), { id: 'sub_aritmatika', mapelId: 'mapel_matematika', nama: 'Aritmatika', isDummy: true });
  batch.set(db.collection('subMateri').doc('sub_aljabar'), { id: 'sub_aljabar', mapelId: 'mapel_matematika', nama: 'Aljabar', isDummy: true });
  batch.set(db.collection('subMateri').doc('sub_pitagoras'), { id: 'sub_pitagoras', mapelId: 'mapel_matematika', nama: 'Pitagoras', isDummy: true });

  // Nilai dengan variasi Tinggi (>=80), Sedang (70-79), Rendah (<70)
  const dummyGrades = [
    { id: 'nil_01', siswaUid: 'siswa_01', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 90, uts: 88, uas: 92, nilaiAkhir: 90.2, nilaiPerTopik: { Aritmatika: 92, Aljabar: 88, Pitagoras: 90 }, isDummy: true },
    { id: 'nil_02', siswaUid: 'siswa_02', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 76, uts: 75, uas: 78, nilaiAkhir: 76.5, nilaiPerTopik: { Aritmatika: 78, Aljabar: 75, Pitagoras: 76 }, isDummy: true },
    { id: 'nil_03', siswaUid: 'siswa_03', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 55, uts: 60, uas: 58, nilaiAkhir: 57.7, nilaiPerTopik: { Aritmatika: 60, Aljabar: 55, Pitagoras: 58 }, isDummy: true },
    { id: 'nil_04', siswaUid: 'siswa_04', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 85, uts: 82, uas: 88, nilaiAkhir: 85.3, nilaiPerTopik: { Aritmatika: 88, Aljabar: 82, Pitagoras: 85 }, isDummy: true },
    { id: 'nil_05', siswaUid: 'siswa_05', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 72, uts: 70, uas: 74, nilaiAkhir: 72.2, nilaiPerTopik: { Aritmatika: 74, Aljabar: 70, Pitagoras: 72 }, isDummy: true },
    { id: 'nil_06', siswaUid: 'siswa_06', kelasId: 'kelas_9a', mapelId: 'mapel_matematika', periodeId: 'periode_ganjil', tugas: 70, uts: 0, uas: 0, nilaiAkhir: null, nilaiPerTopik: { Aritmatika: 70, Aljabar: 0, Pitagoras: 0 }, isDummy: true }
  ];

  for (const g of dummyGrades) {
    batch.set(db.collection('nilai').doc(g.id), g, { merge: true });
    batch.set(db.collection('grades').doc(g.id), g, { merge: true });
  }

  await batch.commit();
  console.log('✅ [SeedManager] Data dummy berhasil dibuat di Auth & Firestore!');
}

async function clearDummyData() {
  console.log(`🧹 [SeedManager] Menghapus HANYA data dummy (isDummy === true)...`);

  const collections = ['pengguna', 'users', 'sekolah', 'tahunAjaran', 'kelas', 'mapel', 'subMateri', 'periode', 'penugasan', 'keanggotaanKelas', 'relasiOrangTua', 'nilai', 'koreksiNilai', 'notifikasi', 'sesiPerangkat', 'insightCache', 'teaching_profiles', 'grades', 'subjects', 'topics', 'insights'];

  for (const colName of collections) {
    try {
      const snapshot = await db.collection(colName).where('isDummy', '==', true).get();
      if (!snapshot.empty) {
        const batch = db.batch();
        snapshot.docs.forEach(doc => batch.delete(doc.ref));
        await batch.commit();
        console.log(`  - Menghapus ${snapshot.size} dokumen dummy dari '${colName}'`);
      }
    } catch (e) {
      console.warn(`  - Warning clearing '${colName}':`, e.message);
    }
  }

  // Clear Auth Dummy Users
  for (const u of DUMMY_USERS) {
    try {
      await auth.deleteUser(u.uid);
      console.log(`  - Menghapus Auth dummy user: ${u.email}`);
    } catch (_) {}
  }

  console.log('✅ [SeedManager] Data dummy berhasil dibersihkan!');
}

async function resetDummyData() {
  await clearDummyData();
  await seedData();
}

// Execution from CLI
if (require.main === module) {
  const action = process.argv[2] || 'seed';
  (async () => {
    try {
      if (action === 'clear' || action === 'seed:clear') {
        await clearDummyData();
      } else if (action === 'reset' || action === 'seed:reset') {
        await resetDummyData();
      } else {
        await seedData();
      }
      process.exit(0);
    } catch (e) {
      console.error('❌ Error executing seed command:', e);
      process.exit(1);
    }
  })();
}

module.exports = {
  seedData,
  clearDummyData,
  resetDummyData
};
