const axios = require('axios');

const FIRESTORE_BASE = 'http://127.0.0.1:8080/v1/projects/demo-kawansekolah/databases/(default)/documents/';
const AUTH_BASE = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';

async function inspectCollection(colName) {
  try {
    const res = await axios.get(`${FIRESTORE_BASE}${colName}`);
    const docs = res.data.documents || [];
    console.log(`\n📚 Firestore Collection [${colName}]: ${docs.length} dokumen ditemukan`);
    docs.slice(0, 5).forEach(doc => {
      const id = doc.name.split('/').pop();
      console.log(`  - Doc ID: ${id}`);
    });
  } catch (e) {
    console.error(`❌ Gagal membaca koleksi [${colName}]:`, e.message);
  }
}

async function main() {
  console.log('======================================================');
  console.log('🔍 DIRECT DATABASE INSPECTION (CMD / REST API)');
  console.log('======================================================');

  await inspectCollection('users');
  await inspectCollection('teaching_profiles');
  await inspectCollection('subjects');
  await inspectCollection('topics');
  await inspectCollection('grades');

  console.log('\n======================================================\n');
}

main();
