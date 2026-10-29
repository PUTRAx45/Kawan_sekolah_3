const axios = require('axios');

const EMULATOR_AUTH_URL = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';

async function testSignIn(email, password) {
  try {
    const res = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-api-key`, {
      email,
      password,
      returnSecureToken: true
    });
    console.log(`✅ Auth SUCCESS for ${email} (UID: ${res.data.localId})`);
    return true;
  } catch (e) {
    const msg = e.response?.data?.error?.message || e.message;
    console.log(`❌ Auth FAILED for ${email}: ${msg}`);
    return false;
  }
}

async function signUpUser(email, password, displayName) {
  try {
    const res = await axios.post(`${EMULATOR_AUTH_URL}accounts:signUp?key=fake-api-key`, {
      email,
      password,
      displayName,
      returnSecureToken: true
    });
    console.log(`✅ Berhasil membuat akun via REST: ${email} (UID: ${res.data.localId})`);
    return true;
  } catch (e) {
    const msg = e.response?.data?.error?.message || e.message;
    if (msg.includes('EMAIL_EXISTS')) {
      console.log(`ℹ️ Akun ${email} sudah ada di Auth Emulator.`);
      return true;
    }
    console.error(`❌ Gagal mendaftarkan ${email}:`, msg);
    return false;
  }
}

async function main() {
  console.log('======================================================');
  console.log('🔍 VERIFIKASI AKUN UJI DI FIREBASE AUTH EMULATOR');
  console.log('======================================================');

  let u1 = await testSignIn('guru@kawansekolah.sch.id', '123456');
  let u2 = await testSignIn('12345@kawansekolah.local', '123456');
  let u3 = await testSignIn('ortu.ahmad@gmail.com', '123456');
  let u4 = await testSignIn('ortu.ahmad@kawansekolah.local', '123456');

  if (!u1 || !u2 || (!u3 && !u4)) {
    console.log('\n⚠️ Menambahkan akun uji ke Auth Emulator...');

    await signUpUser('guru@kawansekolah.sch.id', '123456', 'Drs. Budi Raharjo');
    await signUpUser('12345@kawansekolah.local', '123456', 'Ahmad Budi');
    await signUpUser('ortu.ahmad@gmail.com', '123456', 'Siti Rahma (Orang Tua Ahmad)');
    await signUpUser('ortu.ahmad@kawansekolah.local', '123456', 'Siti Rahma (Orang Tua Ahmad)');

    console.log('\n🔄 Memeriksa kembali status login akun uji:');
    await testSignIn('guru@kawansekolah.sch.id', '123456');
    await testSignIn('12345@kawansekolah.local', '123456');
    await testSignIn('ortu.ahmad@gmail.com', '123456');
    await testSignIn('ortu.ahmad@kawansekolah.local', '123456');
  }

  console.log('======================================================\n');
}

main();
