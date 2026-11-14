package com.example.kawansekolah.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val teacherId: String = "",
    val studentIds: List<String> = emptyList(),
    val schoolLevel: String = "SMP",
    val grade: Int = 9,
    val className: String = "9-A",
    val aktif: Boolean = true
) {
    @get:Exclude
    val userRole: UserRole
        get() = when (role.trim().uppercase()) {
            "ADMIN" -> UserRole.ADMIN
            "GURU", "TEACHER" -> UserRole.GURU
            "WALI_KELAS", "HOMEROOM_TEACHER" -> UserRole.WALI_KELAS
            "ORANG_TUA", "PARENT" -> UserRole.ORANG_TUA
            else -> UserRole.SISWA
        }

    companion object {
        fun isValidLevelAndGrade(schoolLevel: String, grade: Int): Boolean {
            return when (schoolLevel.trim().uppercase()) {
                "SD" -> grade in 1..6
                "SMP" -> grade in 7..9
                "SMA" -> grade in 10..12
                else -> false
            }
        }
    }
}
