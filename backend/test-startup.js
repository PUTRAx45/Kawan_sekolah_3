require('dotenv').config();
const express = require('express');
const app = express();

function isKeyPlaceholder(apiKey) {
  if (!apiKey) return true;
  const keyStr = apiKey.trim().toLowerCase();
  return (
    keyStr === '' ||
    keyStr === 'isi_key_di_sini' ||
    keyStr.includes('your_openrouter_api_key') ||
    keyStr.includes('your_api_key_here') ||
    keyStr.includes('placeholder')
  );
}

const openRouterStatus = isKeyPlaceholder(process.env.OPENROUTER_API_KEY) ? 'Kosong / Placeholder' : 'Terdeteksi';
const isEmulatorMode = Boolean(process.env.FIREBASE_AUTH_EMULATOR_HOST || process.env.FIRESTORE_EMULATOR_HOST);
const firebaseModeLabel = isEmulatorMode ? 'Firebase Emulator (Local Debug)' : 'Firebase Cloud Produksi';

const PORT = 3001;
const server = app.listen(PORT, () => {
  console.log(`======================================================`);
  console.log(`🚀 Aplikasi: Scolarsia AI Analytics Backend`);
  console.log(`📡 Port: ${PORT}`);
  console.log(`🔑 OpenRouter Key Status: ${openRouterStatus}`);
  console.log(`🔥 Firebase Mode: ${firebaseModeLabel}`);
  console.log(`======================================================`);
  server.close(() => {
    process.exit(0);
  });
});
