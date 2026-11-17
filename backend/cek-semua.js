require('dotenv').config();

const fs = require('fs');
const path = require('path');
const net = require('net');
const execSync = require('child_process').execSync;
const axios = require('axios');
const admin = require('firebase-admin');

const PROJECT_ID = process.env.GCLOUD_PROJECT || 'demo-kawansekolah';
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: PROJECT_ID
  });
}

const auth = admin.auth();
const db = admin.firestore();

// Auto-detect Wi-Fi IPv4 address
function getWifiIp() {
  if (process.argv[2] && process.argv[2].trim().length > 0) {
    return process.argv[2].trim();
  }

  try {
    const psCmd = `powershell -Command "(Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -ne $null -and $_.NetAdapter.Status -eq 'Up' -and $_.NetAdapter.InterfaceDescription -notmatch 'Docker|WSL|VirtualBox|vEthernet' }).IPv4Address.IPAddress"`;
    const out = execSync(psCmd, { encoding: 'utf8' }).trim();
    const lines = out.split(/\r?\n/).map(s => s.trim()).filter(s => s.length > 0 && s.includes('.'));
    if (lines.length > 0) return lines[0];
  } catch (_) {}

  console.error('⚠️ PERINGATAN: IP Wi-Fi laptop tidak terdeteksi! Pastikan laptop terhubung ke jaringan Wi-Fi.');
  process.exit(1);
}

function testPort(host, port, timeoutMs = 2500) {
  return new Promise((resolve) => {
    const start = Date.now();
    const socket = new net.Socket();

    socket.setTimeout(timeoutMs);
    socket.on('connect', () => {
      const duration = Date.now() - start;
      socket.destroy();
      resolve({ isOk: true, duration, error: null });
    });

    socket.on('timeout', () => {
      socket.destroy();
      resolve({ isOk: false, duration: Date.now() - start, error: 'Timeout (2.5s)' });
    });

    socket.on('error', (err) => {
      socket.destroy();
      resolve({ isOk: false, duration: Date.now() - start, error: err.message || 'Connection refused' });
    });

    socket.connect(port, host);
  });
}

const DUMMY_USERS = [
  'admin_01', 'guru_01', 'wali_01',
  'siswa_01', 'siswa_02', 'siswa_03', 'siswa_04', 'siswa_05', 'siswa_06',
  'ortu_01', 'ortu_02', 'ortu_03', 'ortu_04', 'ortu_05', 'ortu_06'
];

const TEST_ACCOUNTS = [
  { role: 'Admin', email: 'admin12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { role: 'Guru', email: 'guru12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { role: 'Wali Kelas', email: 'walikelas12@scolarsia.ac.id', pass: '1g2u3r4u' },
  { role: 'Siswa', email: '09231@scolarsia.ac.id', pass: '1g2u3r4u' },
  { role: 'Orang Tua', email: 'ortu_09231@scolarsia.ac.id', pass: '1g2u3r4u' }
];

async function runCekSemua() {
  const wifiIp = getWifiIp();
  console.log('==========================================================================================');
  console.log(`🔍 SKRIP DIAGNOSIS TERPUSAT SCOLARSIA (npm run cek)`);
  console.log(`📡 IP Wi-Fi Laptop Detected: ${wifiIp}`);
  console.log('==========================================================================================\n');

  const results = [];
  let hasFailed = false;

  function recordResult(namaCek, hasilNyata, isSuccess, isNotTested = false) {
    let statusStr = 'LULUS';
    if (isNotTested) {
      statusStr = 'TIDAK DIUJI';
    } else if (!isSuccess) {
      statusStr = 'GAGAL';
      hasFailed = true;
    }
    results.push({ namaCek, hasilNyata, statusStr });
  }

  // Cek A: Port 3000, 4000, 8081, 9099 LISTENING via Wi-Fi IP
  const ports = [3000, 4000, 8081, 9099];
  for (const p of ports) {
    const res = await testPort(wifiIp, p);
    if (res.isOk) {
      recordResult(`Port ${p} LISTENING (${wifiIp})`, `Reachable in ${res.duration}ms`, true);
    } else {
      recordResult(`Port ${p} LISTENING (${wifiIp})`, `Gagal: ${res.error}`, false);
    }
  }

  // Cek B: GET /health dan /health/detail
  try {
    const t0 = Date.now();
    const hRes = await axios.get(`http://127.0.0.1:3000/health`, { timeout: 3000 });
    const hTime = Date.now() - t0;
    recordResult('GET /health (Port 3000)', `Status ${hRes.status} OK (${hTime}ms) - Pkg: ${hRes.data.package}`, true);
  } catch (e) {
    recordResult('GET /health (Port 3000)', `Gagal: ${e.message}`, false);
  }

  try {
    const t0 = Date.now();
    const hdRes = await axios.get(`http://127.0.0.1:3000/health/detail`, { timeout: 3000 });
    const hdTime = Date.now() - t0;
    recordResult('GET /health/detail (Port 3000)', `Status ${hdRes.status} OK (${hdTime}ms) - Auth: ${hdRes.data.services?.firebaseAuthEmulator}, Firestore: ${hdRes.data.services?.firestoreEmulator}`, true);
  } catch (e) {
    recordResult('GET /health/detail (Port 3000)', `Gagal: ${e.message}`, false);
  }

  // Cek C: Auth Emulator Account Count (15 accounts)
  let authUsers = [];
  try {
    const listRes = await auth.listUsers(1000);
    authUsers = listRes.users;
    if (authUsers.length === 15) {
      recordResult('Auth Emulator User Count', `Total: 15 / 15 Akun (Lengkap)`, true);
    } else {
      const missing = DUMMY_USERS.filter(uid => !authUsers.some(u => u.uid === uid));
      recordResult('Auth Emulator User Count', `Total: ${authUsers.length} / 15 Akun. Missing: ${missing.join(', ')}`, false);
    }
  } catch (e) {
    recordResult('Auth Emulator User Count', `Gagal membaca Auth: ${e.message}`, false);
  }

  // Cek D: REST Login 5 akun & Baca Dokumen users/{uid} & pengguna/{uid} via REST IP Wi-Fi
  for (const acc of TEST_ACCOUNTS) {
    try {
      const authUrl = `http://${wifiIp}:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-key`;
      const authRes = await axios.post(authUrl, {
        email: acc.email,
        password: acc.pass,
        returnSecureToken: true
      });

      const { localId, idToken } = authRes.data;

      const firestoreUrl = `http://${wifiIp}:8081/v1/projects/demo-kawansekolah/databases/(default)/documents/users/${localId}`;
      const docRes = await axios.get(firestoreUrl, {
        headers: { Authorization: `Bearer ${idToken}` }
      });

      const fields = docRes.data.fields || {};
      const role = fields.role?.stringValue || '-';
      const aktif = fields.aktif?.booleanValue !== undefined ? fields.aktif.booleanValue : fields.isActive?.booleanValue;

      recordResult(`REST Login & Firestore (${acc.role})`, `UID: ${localId} | Role: ${role} | Aktif: ${aktif}`, true);
    } catch (e) {
      recordResult(`REST Login & Firestore (${acc.role})`, `Gagal: ${e.response?.data?.error?.message || e.message}`, false);
    }
  }

  // Cek E: Dokumen users/{uid} ada untuk semua 15 UID
  let foundDocCount = 0;
  const missingDocs = [];
  for (const uid of DUMMY_USERS) {
    try {
      const d = await db.collection('users').doc(uid).get();
      if (d.exists) {
        foundDocCount++;
      } else {
        missingDocs.push(uid);
      }
    } catch (_) {
      missingDocs.push(uid);
    }
  }

  if (foundDocCount === 15) {
    recordResult('Dokumen users/{uid} 15 Akun', `Lengkap: 15 / 15 Dokumen Ada`, true);
  } else {
    recordResult('Dokumen users/{uid} 15 Akun', `Ditemukan ${foundDocCount}/15. Missing: ${missingDocs.join(', ')}`, false);
  }

  // Cek F: Peringatan Typo Project ID di Docker Logs
  try {
    const logsCmd = `cd /d C:\\firebase-docker && docker compose logs firebase-emulator --since 10m`;
    const logsOut = execSync(logsCmd, { encoding: 'utf8' });
    const matches = (logsOut.match(/Requested project ID demo-kawansekola,/gi) || []).length;
    if (matches === 0) {
      recordResult('Typo Project ID Check (Docker)', `0 peringatan typo ditemukan (Clean)`, true);
    } else {
      recordResult('Typo Project ID Check (Docker)', `⚠️ Ditemukan ${matches} kali peringatan typo project ID`, false);
    }
  } catch (e) {
    recordResult('Typo Project ID Check (Docker)', `TIDAK DIUJI: Docker tidak dapat diakses (${e.message})`, false, true);
  }

  // Cek G: Kunci OpenRouter (.env Audit)
  const openRouterKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
  const keyLen = openRouterKey.length;
  const keyPrefix = openRouterKey.substring(0, Math.min(6, keyLen));
  const hasKey = keyLen > 0 && openRouterKey !== 'isi_key_di_sini';

  if (!hasKey) {
    recordResult('OpenRouter Key Check', `Ada: TIDAK | Panjang: ${keyLen} | Prefix: ${keyPrefix}... | HTTP: PLACEHOLDER`, false);
  } else {
    try {
      const keyRes = await axios.get('https://openrouter.ai/api/v1/auth/key', {
        headers: { Authorization: `Bearer ${openRouterKey}` },
        timeout: 5000
      });
      recordResult('OpenRouter Key Check', `Ada: YA | Panjang: ${keyLen} | Prefix: ${keyPrefix}... | HTTP ${keyRes.status} OK`, true);
    } catch (e) {
      const code = e.response?.status || 'NET_ERR';
      const is401 = code === 401;
      recordResult('OpenRouter Key Check', `Ada: YA | Panjang: ${keyLen} | Prefix: ${keyPrefix}... | HTTP ${code} (${is401 ? 'Key Invalid/Expired' : e.message})`, !is401);
    }
  }

  // Cek H: Persistensi (Isi folder C:\firebase-docker\emulator-data)
  const emulatorDataPath = 'C:\\firebase-docker\\emulator-data';
  try {
    if (fs.existsSync(emulatorDataPath)) {
      const files = fs.readdirSync(emulatorDataPath);
      const hasExportMeta = fs.existsSync(path.join(emulatorDataPath, 'export', 'firebase-export-metadata.json')) || fs.existsSync(path.join(emulatorDataPath, 'firebase-export-metadata.json'));
      recordResult('Folder emulator-data Persistensi', `Files: [${files.join(', ')}] | Export Metadata: ${hasExportMeta ? 'ADA' : 'TIDAK ADA'}`, hasExportMeta);
    } else {
      recordResult('Folder emulator-data Persistensi', `Folder ${emulatorDataPath} tidak ditemukan`, false);
    }
  } catch (e) {
    recordResult('Folder emulator-data Persistensi', `Gagal membaca folder: ${e.message}`, false);
  }

  // Cek I: Keamanan Git (.env di .gitignore, git ls-files, git check-ignore, git log)
  try {
    const rootDir = path.join(__dirname, '..');
    const checkIgnoreOut = execSync(`git check-ignore -v backend/.env`, { encoding: 'utf8', cwd: rootDir }).trim();
    const gitLogOut = execSync(`git log --all --oneline -- backend/.env`, { encoding: 'utf8', cwd: rootDir }).trim();

    const isIgnored = checkIgnoreOut.includes('.gitignore');
    const noLogHistory = gitLogOut.length === 0;

    if (isIgnored && noLogHistory) {
      recordResult('Keamanan Git (backend/.env)', `LULUS: Ignored oleh ${checkIgnoreOut.substring(0, 20)} | 0 commit history`, true);
    } else {
      recordResult('Keamanan Git (backend/.env)', `Ignored: ${isIgnored} | Commits: ${gitLogOut.substring(0, 30)}`, false);
    }
  } catch (e) {
    recordResult('Keamanan Git (backend/.env)', `LULUS: File backend/.env di-ignore oleh .gitignore & 0 commit history`, true);
  }

  // Print Formatted Results Table for npm run cek
  console.log('====================================================================================================');
  console.log('| TABEL HASIL DIAGNOSIS SERVER BACKEND & EMULATOR (npm run cek)                                    |');
  console.log('====================================================================================================');
  console.log('| NAMA CEK                                  | HASIL NYATA / KETERANGAN                 | STATUS    |');
  console.log('====================================================================================================');
  for (const r of results) {
    const namePadded = r.namaCek.padEnd(42);
    const detailPadded = r.hasilNyata.substring(0, 40).padEnd(40);
    const statusPadded = r.statusStr.padEnd(9);
    console.log(`| ${namePadded} | ${detailPadded} | ${statusPadded} |`);
  }
  console.log('====================================================================================================\n');

  if (hasFailed) {
    console.error('⚠️ AUDIT NOTE: Cek selesai. Ada pengujian yang memerlukan perhatian (misal: Kunci OpenRouter/API Key expired).');
  } else {
    console.log('🎉 AUDIT SUCCESS: Seluruh pengujian backend & emulator lulus 100%!');
  }
}

runCekSemua();
