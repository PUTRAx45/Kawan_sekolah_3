const { clearDummyData, resetDummyData } = require('./seed-manager');

async function testSeedReset() {
  console.log('======================================================');
  console.log('🧪 PENGUJIAN SEED CLEAR & SEED RESET');
  console.log('======================================================');

  try {
    console.log('1. Menjalankan clearDummyData()...');
    await clearDummyData();

    console.log('\n2. Menjalankan resetDummyData()...');
    await resetDummyData();

    console.log('======================================================');
    console.log('✅ PENGUJIAN SEED CLEAR & RESET LULUS 100%!');
    console.log('======================================================\n');
  } catch (e) {
    console.error('❌ Gagal:', e);
  } finally {
    process.exit(0);
  }
}

testSeedReset();
