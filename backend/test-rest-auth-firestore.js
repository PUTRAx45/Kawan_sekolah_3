const axios = require('axios');

const HOST_IP = '192.168.1.4';
const AUTH_REST_URL = `http://${HOST_IP}:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-key`;

const ACCOUNTS = [
  { label: 'Admin', email: 'admin12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { label: 'Guru', email: 'guru12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { label: 'Wali Kelas', email: 'walikelas12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { label: 'Siswa', email: '09231@scolarsia.ac.id', pass: '1g2u3r4u' },
  { label: 'Orang Tua', email: 'ortu_09231@scolarsia.ac.id', pass: '1g2u3r4u' }
];

async function testRestAuthAndFirestore() {
  console.log('======================================================');
  console.log(`🌐 PENGUJIAN REST AUTH (port 9099) & FIRESTORE (port 8081) VIA IP: ${HOST_IP}`);
  console.log('======================================================');

  for (const acc of ACCOUNTS) {
    try {
      // 1. REST Auth signInWithPassword
      const authRes = await axios.post(AUTH_REST_URL, {
        email: acc.email,
        password: acc.pass,
        returnSecureToken: true
      });

      const { localId, idToken } = authRes.data;

      // 2. REST Firestore read users/{uid}
      const firestoreUrl = `http://${HOST_IP}:8081/v1/projects/demo-kawansekolah/databases/(default)/documents/users/${localId}`;
      const docRes = await axios.get(firestoreUrl, {
        headers: { Authorization: `Bearer ${idToken}` }
      });

      const fields = docRes.data.fields || {};
      const role = fields.role?.stringValue || '-';
      const aktif = fields.aktif?.booleanValue !== undefined ? fields.aktif.booleanValue : fields.isActive?.booleanValue;

      console.log(`✅ [${acc.label.padEnd(10)}] Email: ${acc.email.padEnd(28)} | localId: ${localId.padEnd(10)} | Role: ${role.padEnd(10)} | Aktif: ${aktif}`);
    } catch (e) {
      console.error(`❌ [${acc.label.padEnd(10)}] Email: ${acc.email} -> Error:`, e.response?.data?.error?.message || e.message);
    }
  }

  console.log('======================================================\n');
}

testRestAuthAndFirestore();
