package com.example.kawansekolah.data.model

import com.google.firebase.firestore.Exclude

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT", // "TEACHER", "STUDENT", "PARENT"
    val teacherId: String = "",
    val studentIds: List<String> = emptyList(),
    val schoolLevel: String = "SMP", // SD, SMP, SMA
    val grade: Int = 9,
    val className: String = "9-A"
) {
    @get:Exclude
    val userRole: UserRole
        get() = try {
            UserRole.valueOf(role)
        } catch (_: Exception) {
            UserRole.SISWA
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
