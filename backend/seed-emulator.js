/**
 * @deprecated SKRIP LEGACY / USANG
 * Skrip ini tidak terpakai oleh 'npm run seed'.
 * Skrip seed resmi proyek yang aktif dan terintegrasi adalah 'seed-manager.js'.
 */
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';

const admin = require('firebase-admin');

const PROJECT_ID = 'demo-kawansekolah';

if (!admin.apps.length) {
  admin.initializeApp({ projectId: PROJECT_ID });
}
