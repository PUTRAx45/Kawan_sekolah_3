package com.example.kawansekolah.domain

object AccessPolicy {

    fun isGuruMapel(role: String): Boolean {
        val r = role.trim().uppercase()
        return r == "GURU" || r == "GURU_MAPEL" || r == "TEACHER"
    }

    fun isWaliKelas(role: String): Boolean {
        val r = role.trim().uppercase()
        return r == "WALI_KELAS" || r == "HOMEROOM_TEACHER"
    }

    fun canInputScore(userRole: String, userId: String, assignedTeacherId: String): Boolean {
        if (!isGuruMapel(userRole)) {
            return false
        }
        return userId.isNotBlank() && userId == assignedTeacherId
    }

    fun canViewClassScores(
        userRole: String,
        userId: String,
        classHomeroomTeacherId: String,
        assignedTeacherId: String
    ): Boolean {
        if (isWaliKelas(userRole)) {
            return userId.isNotBlank() && userId == classHomeroomTeacherId
        }

        if (isGuruMapel(userRole)) {
            return userId.isNotBlank() && userId == assignedTeacherId
        }

        return false
    }

    fun canUploadCsv(userRole: String, userId: String, assignedTeacherId: String): Boolean {
        return canInputScore(userRole, userId, assignedTeacherId)
    }
}
