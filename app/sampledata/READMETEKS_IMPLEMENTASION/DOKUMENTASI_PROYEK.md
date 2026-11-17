# Dokumentasi & Panduan Terpusat Proyek Scolarsia

Seluruh berkas catatan, panduan, dan dokumentasi proyek disimpan secara terpusat di direktori ini (`app/sampledata/READMETEKS_IMPLEMENTASION/`).

---

## 📌 Ringkasan Arsitektur Proyek Scolarsia

1. **Aplikasi Android (`app/`)**:
   - Bahasa & Framework: Kotlin, Jetpack Compose, Material 3, Navigation Compose.
   - 5 Peran Pengguna: **Admin**, **Guru**, **Wali Kelas**, **Siswa**, **Orang Tua**.
   - Sistem Warna: Primary Blue (`#1C64F2`), Soft Blue (`#EBF2FE`), Navy Container (`#15325B`), Dark Text (`#111928`), Card Border (`#E5E7EB`).
   - Mode Terang & Gelap (*Dark Mode*) beradaptasi dinamis menggunakan token `MaterialTheme.colorScheme`.
   - Skala Tipografi: `.sp` dinamis mengikuti *System Font Scale* perangkat HP.

2. **Backend API & AI Agent (`backend/`)**:
   - Framework: Express.js (Node.js) di port `3000`.
   - AI Agent: Agent Hermes / OpenRouter API (`openai/gpt-4o-mini`).
   - Moderasi & Deteksi Salam: Memilih balasan salam murni dan menyaring bahasa kasar / pertanyaan di luar kurikulum.

3. **Firebase Local Emulator Suite & Docker (`C:\firebase-docker`)**:
   - Firebase Auth Emulator: Port `9099` (Host `0.0.0.0`).
   - Cloud Firestore Emulator: Port `8081` (Host `0.0.0.0`).
   - Firebase UI Dashboard: Port `4000` (Host `0.0.0.0`).
   - Data Persistence: Menyimpan seluruh data Auth & Firestore di `./emulator-data` saat `docker compose down`.

---

## 🔑 Kredensial Uji Dummy (Kata Sandi Semua Akun: `1g2u3r4u`)

- **Admin:** `admin12@scolarsia.ac.id`
- **Guru:** `guru12@scolarsia.ac.id`
- **Wali Kelas:** `walikelas12@scolarsia.ac.id`
- **Siswa:** `09231@scolarsia.ac.id` s/d `09236@scolarsia.ac.id`
- **Orang Tua:** `ortu_09231@scolarsia.ac.id` s/d `ortu_09236@scolarsia.ac.id`

---

## 🗺️ ROADMAP MENUJU PRODUKSI & DEPLOYMENT

1. **Langkah 1: Selesaikan Lokal (Local Completion & Verification)**
   - Login 5 peran di HP Fisik (Admin, Guru, Wali Kelas, Siswa, Orang Tua).
   - Penguncian peran (1 akun = 1 peran) & isolasi navigasi.
   - Fitur AI Analytics & Chat berjalan lancar.

2. **Langkah 2: Uji Aturan Keamanan (Security Rules)**
   - Menjalankan `npm run test-rules` di folder `backend/` untuk menguji isolasi hak akses Firestore per peran.

3. **Langkah 3: Firebase Cloud Production & Akun Admin Awal**
   - Membuat project Firebase Cloud asli di Firebase Console.
   - Mengunduh `google-services.json` produksi dan mendaftarkan akun Admin pertama.

4. **Langkah 4: Deploy Backend AI ke Cloud Hosting (HTTPS & Secrets)**
   - Mempublikasikan service `backend/` ke platform cloud (Render / Railway / Koyeb) dengan variabel lingkungan `OPENROUTER_API_KEY` & `FIREBASE_SERVICE_ACCOUNT_JSON`.

5. **Langkah 5: Build Release APK**
   - Membangun APK varian `release` (tanpa dialog IP server, tanpa dev seed, tanpa cleartext HTTP).

6. **Langkah 6: Kebijakan Privasi & Rilis Play Store**
   - Menyusun Kebijakan Privasi (*Privacy Policy*) perlindungan data siswa & sekolah sebelum publikasi di Google Play Store.

---

## 🚀 Perintah Pengembang (NPM Scripts)

Di folder `backend/`:
- `npm start`: Menjalankan server backend Express di port 3000.
- `npm run cek`: Menjalankan 17 sub-cek diagnosis terpusat server & emulator.
- `npm run seed`: Mengisi data dummy ke emulator Auth & Firestore.
- `npm run seed:clear`: Menghapus **hanya** data dummy (`isDummy === true`).
- `npm run seed:reset`: Menghapus data dummy lalu mengisinya kembali.
- `npm run test-rules`: Menguji aturan `firestore.rules`.
- `npm run test-ai`: Menguji endpoint AI Chat & Insight.
