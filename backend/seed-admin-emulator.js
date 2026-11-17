/**
 * @deprecated SKRIP LEGACY / USANG
 * Skrip ini tidak terpakai oleh 'npm run seed'.
 * Skrip seed resmi proyek yang aktif dan terintegrasi adalah 'seed-manager.js'.
 */
const admin = require('firebase-admin');

process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: process.env.FIREBASE_PROJECT_ID || 'demo-kawansekolah'
  });
}

const db = admin.firestore();

async function seedAdminEmulator() {
  console.log('======================================================');
  console.log('🌱 SEEDING ADMIN EMULATOR (LEGACY)');
  console.log('======================================================');
}

seedAdminEmulator();
