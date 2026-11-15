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

## 🚀 Perintah Pengembang (NPM Scripts)

Di folder `backend/`:
- `npm start`: Menjalankan server backend Express di port 3000.
- `npm run seed`: Mengisi data dummy ke emulator Auth & Firestore.
- `npm run seed:clear`: Menghapus **hanya** data dummy (`isDummy === true`).
- `npm run seed:reset`: Menghapus data dummy lalu mengisinya kembali.
- `npm run test-rules`: Menguji aturan `firestore.rules`.
- `npm run test-ai`: Menguji endpoint AI Chat & Insight.
