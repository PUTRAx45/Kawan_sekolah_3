const axios = require('axios');

const EMULATOR_AUTH_URL = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';

async function testSignIn(roleLabel, email, password) {
  try {
    const res = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-api-key`, {
      email,
      password,
      returnSecureToken: true
    });
    console.log(`✅ [LULUS] Akun ${roleLabel} (${email}) -> Auth UID: ${res.data.localId}`);
    return true;
  } catch (e) {
    const msg = e.response?.data?.error?.message || e.message;
    console.log(`❌ [GAGAL] Akun ${roleLabel} (${email}): ${msg}`);
    return false;
  }
}

async function main() {
  console.log('======================================================');
  console.log('🔍 VERIFIKASI PENGUJIAN LOGIN 5 AKUN DUMMY SCOLARSIA');
  console.log('======================================================');

  await testSignIn('Admin', 'admin12@scolarsia.ac.id', '1g2u3r4u');
  await testSignIn('Guru', 'guru12@scolarsia.ac.id', '1g2u3r4u');
  await testSignIn('Wali Kelas', 'walikelas12@scolarsia.ac.id', '1g2u3r4u');
  await testSignIn('Siswa', '09231@scolarsia.ac.id', '1g2u3r4u');
  await testSignIn('Orang Tua', 'ortu_09231@scolarsia.ac.id', '1g2u3r4u');

  console.log('======================================================\n');
}

main();
