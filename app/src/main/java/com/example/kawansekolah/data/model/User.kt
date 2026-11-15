package com.example.kawansekolah.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class User(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("nama") @set:PropertyName("nama") var name: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "SISWA",
    @get:PropertyName("teacherId") @set:PropertyName("teacherId") var teacherId: String = "",
    @get:PropertyName("studentIds") @set:PropertyName("studentIds") var studentIds: List<String> = emptyList(),
    @get:PropertyName("schoolLevel") @set:PropertyName("schoolLevel") var schoolLevel: String = "SMP",
    @get:PropertyName("grade") @set:PropertyName("grade") var grade: Int = 9,
    @get:PropertyName("className") @set:PropertyName("className") var className: String = "9-A",
    @get:PropertyName("aktif") @set:PropertyName("aktif") var aktif: Boolean = true
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
