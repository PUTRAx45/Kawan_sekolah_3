const axios = require('axios');

const FIRESTORE_URL = 'http://127.0.0.1:8080/v1/projects/demo-kawansekolah/databases/(default)/documents/';

function toFirestoreFields(obj) {
  const fields = {};
  for (const [key, val] of Object.entries(obj)) {
    if (typeof val === 'string') {
      fields[key] = { stringValue: val };
    } else if (typeof val === 'number') {
      if (Number.isInteger(val)) {
        fields[key] = { integerValue: val.toString() };
      } else {
        fields[key] = { doubleValue: val };
      }
    } else if (typeof val === 'boolean') {
      fields[key] = { booleanValue: val };
    } else if (Array.isArray(val)) {
      fields[key] = {
        arrayValue: {
          values: val.map(v => ({ stringValue: String(v) }))
        }
      };
    }
  }
  return fields;
}

async function createDoc(collection, docId, data) {
  try {
    const url = `${FIRESTORE_URL}${collection}/${docId}`;
    const fields = toFirestoreFields(data);
    await axios.patch(url, { fields });
    console.log(`✅ Firestore doc created: ${collection}/${docId}`);
  } catch (e) {
    console.error(`❌ Firestore doc error ${collection}/${docId}:`, e.message);
  }
}

async function getAuthUid(email, password) {
  const url = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-api-key';
  try {
    const res = await axios.post(url, { email, password, returnSecureToken: true });
    return res.data.localId;
  } catch (_) {
    return null;
  }
}

async function main() {
  console.log('======================================================');
  console.log('🌱 SEED DATA AWAL FIRESTORE EMULATOR (NILAI AWAL = 0.0)');
  console.log('======================================================');

  const teacherUid = await getAuthUid('guru@kawansekolah.sch.id', '123456') || 'O6vkbGDTgvBAKjtpj5LdOvnWO1wY';
  const studentUid = await getAuthUid('12345@kawansekolah.local', '123456') || '3dvXsUYZhjFbXasyrMXSDGwNq2zN';
  const parentUid = await getAuthUid('ortu.ahmad@kawansekolah.local', '123456') || 'OMkZmlnfG1zSnLZgFqz9m0eRXvAy';

  const uids = [studentUid, 'siswa_12345'];
  const teacherUids = [teacherUid, 'guru_smp_01'];

  // 1. Users
  for (const tUid of teacherUids) {
    await createDoc('users', tUid, {
      uid: tUid,
      name: 'Drs. Budi Raharjo',
      email: 'guru@kawansekolah.sch.id',
      role: 'TEACHER',
      teacherId: tUid
    });

    await createDoc('teaching_profiles', tUid, {
      teacherId: tUid,
      fullName: 'Drs. Budi Raharjo',
      schoolName: 'SMP Negeri 1 Jakarta',
      schoolLevel: 'SMP',
      classesTaught: ['9-A', '9-B'],
      profileComplete: true
    });
  }

  for (const sUid of uids) {
    await createDoc('users', sUid, {
      uid: sUid,
      name: 'Ahmad Budi',
      email: '12345@kawansekolah.local',
      role: 'STUDENT',
      teacherId: teacherUid,
      schoolLevel: 'SMP',
      grade: 9,
      className: '9-A'
    });
  }

  await createDoc('users', parentUid, {
    uid: parentUid,
    name: 'Siti Rahma (Orang Tua Ahmad)',
    email: 'ortu.ahmad@kawansekolah.local',
    role: 'PARENT',
    teacherId: teacherUid,
    studentIds: uids
  });

  // 2. Subjects & Topics List (Initial score = 0.0)
  const subjectsData = [
    { id: 'subj_agama_9', name: 'Pendidikan Agama dan Budi Pekerti', topics: [
      { id: 'top_akhlak', name: 'Akhlak & Budi Pekerti', weight: 1.0 },
      { id: 'top_ibadah', name: 'Fikih & Ibadah', weight: 1.0 }
    ]},
    { id: 'subj_pancasila_9', name: 'Pendidikan Pancasila', topics: [
      { id: 'top_uud', name: 'Konstitusi & UUD 1945', weight: 1.0 },
      { id: 'top_pancasila_val', name: 'Nilai-Nilai Pancasila', weight: 1.0 }
    ]},
    { id: 'subj_bindo_9', name: 'Bahasa Indonesia', topics: [
      { id: 'top_lho', name: 'Teks LHO', weight: 1.0 },
      { id: 'top_puisi', name: 'Puisi & Majas', weight: 1.0 }
    ]},
    { id: 'subj_matematika_9', name: 'Matematika', topics: [
      { id: 'top_aritmatika', name: 'Aritmatika', weight: 1.0 },
      { id: 'top_geometri', name: 'Geometri', weight: 2.0 },
      { id: 'top_pitagoras', name: 'Pitagoras', weight: 1.5 }
    ]},
    { id: 'subj_ipa_9', name: 'Ilmu Pengetahuan Alam (IPA)', topics: [
      { id: 'top_kinematika', name: 'Kinematika Gerak', weight: 1.0 },
      { id: 'top_sel', name: 'Struktur Sel & Jaringan', weight: 1.5 }
    ]},
    { id: 'subj_ips_9', name: 'Ilmu Pengetahuan Sosial (IPS)', topics: [
      { id: 'top_kemerdekaan', name: 'Masa Kemerdekaan', weight: 1.0 },
      { id: 'top_ekonomi', name: 'Kegiatan Ekonomi', weight: 1.0 }
    ]},
    { id: 'subj_bing_9', name: 'Bahasa Inggris', topics: [
      { id: 'top_reading', name: 'Reading Comprehension', weight: 1.0 },
      { id: 'top_grammar', name: 'Grammar & Tenses', weight: 1.5 }
    ]},
    { id: 'subj_pjok_9', name: 'PJOK', topics: [
      { id: 'top_senam', name: 'Kebugaran Jasmani', weight: 1.0 },
      { id: 'top_bola', name: 'Permainan Bola Besar', weight: 1.0 }
    ]},
    { id: 'subj_informatika_9', name: 'Informatika', topics: [
      { id: 'top_algoritma', name: 'Algoritma & Pemrograman', weight: 1.5 },
      { id: 'top_jaringan', name: 'Jaringan Komputer', weight: 1.0 }
    ]},
    { id: 'subj_seni_9', name: 'Seni dan Prakarya', topics: [
      { id: 'top_seni_rupa', name: 'Seni Rupa & Lukis', weight: 1.0 },
      { id: 'top_prakarya', name: 'Kerajinan Tangan', weight: 1.0 }
    ]},
    { id: 'subj_mulok_9', name: 'Muatan Lokal', topics: [
      { id: 'top_mulok_budaya', name: 'Budaya Daerah & Bahasa Daerah', weight: 1.0 }
    ]}
  ];

  for (const subj of subjectsData) {
    await createDoc('subjects', subj.id, {
      id: subj.id,
      teacherId: teacherUid,
      name: subj.name,
      schoolLevel: 'SMP',
      grade: 9
    });

    for (const top of subj.topics) {
      await createDoc('topics', top.id, {
        id: top.id,
        teacherId: teacherUid,
        subjectId: subj.id,
        name: top.name,
        weight: top.weight
      });

      for (const sUid of uids) {
        await createDoc('grades', `${sUid}_${top.id}`, {
          id: `${sUid}_${top.id}`,
          studentId: sUid,
          teacherId: teacherUid,
          subjectId: subj.id,
          subjectName: subj.name,
          topicId: top.id,
          topicName: top.name,
          score: 0.0
        });
      }
    }
  }

  console.log('======================================================\n');
}

main();
