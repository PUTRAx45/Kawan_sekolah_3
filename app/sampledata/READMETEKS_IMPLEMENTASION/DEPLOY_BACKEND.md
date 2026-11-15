# Panduan Deploy Backend Scolarsia AI Analytics

Panduan singkat ini menjelaskan langkah-langkah mempublikasikan service Backend AI Scolarsia ke platform hosting cloud (seperti Render, Railway, Fly.io, Vercel, Koyeb, atau VPS Docker).

---

## 1. Variabel Lingkungan (Environment Variables)

Saat melakukan penyiapan di platform hosting, pastikan Anda menambahkan variabel lingkungan berikut pada dashboard hosting:

| Variabel | Wajib | Contoh Nilai / Deskripsi |
| :--- | :--- | :--- |
| `PORT` | Ya (Otomatis oleh hosting) | `3000` atau sesuai port dari hosting |
| `NODE_ENV` | Ya | `production` |
| `OPENROUTER_API_KEY` | Ya | `sk-or-v1-xxxxxxxxxxxxxxxxx` (API Key dari OpenRouter) |
| `OPENROUTER_MODEL` | Opsional | `openai/gpt-4o-mini` (Model bawaan AI) |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Ya (Produksi) | String JSON dari file *Service Account Key* Firebase Admin SDK (dapat berupa string JSON utuh atau string yang di-encode Base64). |
| `FIREBASE_PROJECT_ID` | Ya | ID project Firebase Anda (misal `scolarsia-prod`) |

---

## 2. Cara Mengisi `FIREBASE_SERVICE_ACCOUNT_JSON`

1. Buka [Firebase Console](https://console.firebase.google.com/) -> Pilih Project Anda.
2. Buka **Project Settings** (ikon roda gigi) -> Tab **Service Accounts**.
3. Klik **Generate new private key** -> File JSON akan terunduh.
4. Buka file JSON tersebut, lalu salin **seluruh isinya sebagai 1 baris string**, atau lakukan konversi ke Base64 (contoh: `base64 -w 0 service-account.json`).
5. Tempelkan string tersebut ke variabel lingkungan `FIREBASE_SERVICE_ACCOUNT_JSON` di dashboard hosting.

---

## 3. Langkah Deploy

### Opsi A: Deploy lewat GitHub ke Render / Railway / Koyeb
1. Push folder `backend` atau repository ke GitHub.
2. Buat **Web Service** baru di platform hosting.
3. Sambungkan ke repository GitHub Anda dan arahkan Root Directory ke `backend` (jika berupa monorepo).
4. Biarkan Build Command menggunakan `npm install` dan Start Command menggunakan `npm start`.
5. Isikan semua Environment Variables di atas.
6. Klik **Deploy**.

### Opsi B: Deploy Menggunakan Docker Container
Jika menggunakan Docker (Fly.io, AWS App Runner, VPS, dll.):
```bash
cd backend
docker build -t scolarsia-backend .
docker run -p 3000:3000 -e OPENROUTER_API_KEY="sk-or-v1-..." -e FIREBASE_SERVICE_ACCOUNT_JSON='{...}' scolarsia-backend
```

---

## 4. Memeriksa Status Setelah Deploy

Setelah layanan selesai dibangun dan berjalan, periksa kesehatan server dengan membuka URL publik Anda di browser atau menggunakan cURL:

```bash
curl https://GANTI-DENGAN-URL-BACKEND/health
```

**Respon Sukses yang Diharapkan (`HTTP 200 OK`):**
```json
{
  "status": "ok",
  "service": "Scolarsia AI Backend",
  "timestamp": 1741234567890,
  "uptimeSeconds": 120
}
```

Setelah mendapatkan URL backend publik yang aktif, salin URL HTTPS tersebut dan masukkan ke `BASE_URL` di konfigurasi Android (`app/build.gradle.kts` pada varian `physicalDebug` dan `release`).
