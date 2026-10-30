package com.example.kawansekolah

import com.example.kawansekolah.data.model.GradeEntity
import com.example.kawansekolah.data.model.TopicEntity
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AcademicRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicAnalyticsTest {

    @Test
    fun testUserRoleLabels() {
        assertEquals("Siswa", UserRole.SISWA.label)
        assertEquals("Guru Mapel", UserRole.GURU.label)
        assertEquals("Wali Kelas", UserRole.WALI_KELAS.label)
        assertEquals("Orang Tua", UserRole.ORANG_TUA.label)
    }

    @Test
    fun testSchoolLevelAndGradeValidation() {
        assertTrue(User.isValidLevelAndGrade("SD", 1))
        assertTrue(User.isValidLevelAndGrade("SD", 6))
        assertTrue(User.isValidLevelAndGrade("SMP", 7))
        assertTrue(User.isValidLevelAndGrade("SMP", 9))
        assertTrue(User.isValidLevelAndGrade("SMA", 10))
        assertTrue(User.isValidLevelAndGrade("SMA", 12))

        assertFalse(User.isValidLevelAndGrade("SMK", 10))
        assertFalse(User.isValidLevelAndGrade("SD", 7))
        assertFalse(User.isValidLevelAndGrade("SMP", 6))
    }

    @Test
    fun testWeightedFinalScoreCalculation() {
        val mathSubjectId = "subj_matematika_9"
        val studentId = "siswa_12345"
        val teacherId = "guru_smp_01"

        val topics = listOf(
            TopicEntity("top_aritmatika", teacherId, mathSubjectId, "Aritmatika", 1.0),
            TopicEntity("top_geometri", teacherId, mathSubjectId, "Geometri", 2.0),
            TopicEntity("top_pitagoras", teacherId, mathSubjectId, "Pitagoras", 1.5)
        )

        val grades = listOf(
            GradeEntity("g1", studentId, teacherId, mathSubjectId, "Matematika", "top_aritmatika", "Aritmatika", 90.0),
            GradeEntity("g2", studentId, teacherId, mathSubjectId, "Matematika", "top_geometri", "Geometri", 90.0),
            GradeEntity("g3", studentId, teacherId, mathSubjectId, "Matematika", "top_pitagoras", "Pitagoras", 70.0)
        )

        val (finalScore, weakest, strongest) = AcademicRepository.calculateWeightedFinalScore(topics, grades)

        assertEquals(83.33, finalScore, 0.1)
        assertEquals("Pitagoras", weakest)
        assertTrue(strongest == "Aritmatika" || strongest == "Geometri")
    }
}
