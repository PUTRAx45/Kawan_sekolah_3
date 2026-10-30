package com.example.kawansekolah.data.model

enum class UserRole(val label: String, val subtitle: String) {
    SISWA("Siswa", "Murid / Pelajar"),
    GURU("Guru Mapel", "Pengajar Mata Pelajaran"),
    WALI_KELAS("Wali Kelas", "Wali Murid Kelas"),
    ORANG_TUA("Orang Tua", "Orang Tua / Wali Murid")
}
