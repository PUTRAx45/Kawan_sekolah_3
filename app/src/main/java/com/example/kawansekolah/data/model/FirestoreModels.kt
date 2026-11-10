package com.example.kawansekolah.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

object FirestoreCollections {
    const val SEKOLAH = "sekolah"
    const val PENGGUNA = "pengguna"
    const val TAHUN_AJARAN = "tahunAjaran"
    const val KELAS = "kelas"
    const val MAPEL = "mapel"
    const val SUB_MATERI = "subMateri"
    const val PENUGASAN = "penugasan"
    const val KEANGGOTAAN_KELAS = "keanggotaanKelas"
    const val RELASI_ORANG_TUA = "relasiOrangTua"
    const val PERIODE = "periode"
    const val NILAI = "nilai"
    const val KOREKSI_NILAI = "koreksiNilai"
    const val NOTIFIKASI = "notifikasi"
    const val SESI_PERANGKAT = "sesiPerangkat"
    const val INSIGHT_CACHE = "insightCache"
}

object AppRoles {
    const val ADMIN = "admin"
    const val GURU = "guru"
    const val WALI_KELAS = "wali_kelas"
    const val SISWA = "siswa"
    const val ORANG_TUA = "orang_tua"

    fun isValidRole(role: String): Boolean {
        return role in listOf(ADMIN, GURU, WALI_KELAS, SISWA, ORANG_TUA)
    }
}

@IgnoreExtraProperties
data class Sekolah(
    var id: String = "",
    var nama: String = "",
    var alamat: String = "",
    var adminUid: String = "",
    var createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class Pengguna(
    var uid: String = "",
    var sekolahId: String = "",
    var nama: String = "",
    var role: String = AppRoles.SISWA,
    var email: String = "",
    var nis: String = "",
    var noHp: String = "",
    var aktif: Boolean = true,
    var wajibGantiSandi: Boolean = false
)

@IgnoreExtraProperties
data class TahunAjaran(
    var id: String = "",
    var sekolahId: String = "",
    var nama: String = "",
    var aktif: Boolean = true
)

@IgnoreExtraProperties
data class KelasModel(
    var id: String = "",
    var sekolahId: String = "",
    var jenjang: String = "SMP",
    var nama: String = "",
    var waliKelasUid: String = ""
)

@IgnoreExtraProperties
data class MapelModel(
    var id: String = "",
    var sekolahId: String = "",
    var nama: String = "",
    var jenjang: String = "SMP"
)

@IgnoreExtraProperties
data class SubMateriModel(
    var id: String = "",
    var mapelId: String = "",
    var nama: String = ""
)

@IgnoreExtraProperties
data class PenugasanModel(
    var id: String = "",
    var guruUid: String = "",
    var mapelId: String = "",
    var kelasId: String = "",
    var periodeId: String = ""
)

@IgnoreExtraProperties
data class KeanggotaanKelas(
    var id: String = "",
    var siswaUid: String = "",
    var kelasId: String = ""
)

@IgnoreExtraProperties
data class RelasiOrangTua(
    var id: String = "",
    var orangTuaUid: String = "",
    var siswaUid: String = ""
)

@IgnoreExtraProperties
data class PeriodeModel(
    var id: String = "",
    var sekolahId: String = "",
    var nama: String = "",
    var aktif: Boolean = true,
    var bobotTugas: Double = 30.0,
    var bobotUts: Double = 30.0,
    var bobotUas: Double = 40.0
)

@IgnoreExtraProperties
data class NilaiModel(
    var id: String = "",
    var siswaUid: String = "",
    var kelasId: String = "",
    var mapelId: String = "",
    var periodeId: String = "",
    var tugas: Double = 0.0,
    var uts: Double = 0.0,
    var uas: Double = 0.0,
    var nilaiAkhir: Double? = null,
    var nilaiPerTopik: Map<String, Double> = emptyMap()
)

@IgnoreExtraProperties
data class KoreksiNilaiModel(
    var id: String = "",
    var nilaiId: String = "",
    var guruUid: String = "",
    var alasan: String = "",
    var nilaiLama: Double = 0.0,
    var nilaiBaru: Double = 0.0,
    var timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class NotifikasiModel(
    var id: String = "",
    var targetUid: String = "",
    var judul: String = "",
    var pesan: String = "",
    var dibaca: Boolean = false,
    var timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class SesiPerangkatModel(
    var id: String = "",
    var userUid: String = "",
    var namaPerangkat: String = "",
    var terakhirAktif: Long = System.currentTimeMillis(),
    var aktif: Boolean = true
)

@IgnoreExtraProperties
data class InsightCacheModel(
    var id: String = "",
    var targetUid: String = "",
    var targetType: String = "",
    var ringkasan: String = "",
    var rekomendasi: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
