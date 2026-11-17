const axios = require('axios');
const execSync = require('child_process').execSync;

function getWifiIp() {
  if (process.argv[2] && process.argv[2].trim().length > 0) {
    return process.argv[2].trim();
  }
  if (process.env.SERVER_IP && process.env.SERVER_IP.trim().length > 0) {
    return process.env.SERVER_IP.trim();
  }
  try {
    const psCmd = `powershell -Command "(Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' }).IPv4Address.IPAddress"`;
    const out = execSync(psCmd, { encoding: 'utf8' }).trim();
    const lines = out.split(/\r?\n/).map(s => s.trim()).filter(s => s.length > 0 && s.includes('.'));
    if (lines.length > 0) return lines[0];
  } catch (_) {}

  console.error('⚠️ PERINGATAN: IP Wi-Fi laptop tidak terdeteksi! Masukkan IP via argumen (node test-rest-auth-firestore.js 192.168.x.x) atau atur env SERVER_IP.');
  process.exit(1);
}

async function testRestAuthAndFirestore() {
  const HOST_IP = getWifiIp();
  const AUTH_REST_URL = `http://${HOST_IP}:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-key`;

  const ACCOUNTS = [
    { label: 'Admin', email: 'admin12@scolarsia.ac.id', pass: '1g2u3r4u' },
    { label: 'Guru', email: 'guru12@scolarsia.ac.id', pass: '1g2u3r4u' },
    { label: 'Wali Kelas', email: 'walikelas12@scolarsia.ac.id', pass: '1g2u3r4u' },
    { label: 'Siswa', email: '09231@scolarsia.ac.id', pass: '1g2u3r4u' },
    { label: 'Orang Tua', email: 'ortu_09231@scolarsia.ac.id', pass: '1g2u3r4u' }
  ];

  console.log('======================================================');
  console.log(`🌐 PENGUJIAN REST AUTH (port 9099) & FIRESTORE (port 8081) VIA IP: ${HOST_IP}`);
  console.log('======================================================');

  for (const acc of ACCOUNTS) {
    try {
      const authRes = await axios.post(AUTH_REST_URL, {
        email: acc.email,
        password: acc.pass,
        returnSecureToken: true
      });

      const { localId, idToken } = authRes.data;

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
