package com.example.kawansekolah.data.model

data class SubjectEntity(
    val id: String = "",
    val teacherId: String = "",
    val name: String = "",
    val schoolLevel: String = "SMP",
    val grade: Int = 9
)

data class TopicEntity(
    val id: String = "",
    val teacherId: String = "",
    val subjectId: String = "",
    val name: String = "",
    val weight: Double = 1.0
)

data class GradeEntity(
    val id: String = "",
    val studentId: String = "",
    val teacherId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val topicId: String = "",
    val topicName: String = "",
    val score: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class AiInsightEntity(
    val id: String = "",
    val studentId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val finalScore: Double = 0.0,
    val weakestTopic: String = "",
    val strongestTopic: String = "",
    val recommendation: String = "",
    val generatedAt: Long = System.currentTimeMillis()
)

data class SubjectWithDetails(
    val subject: SubjectEntity,
    val topics: List<TopicEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val weightedFinalScore: Double? = null,
    val weakestTopic: String? = null,
    val aiInsight: AiInsightEntity? = null
)

// === MODEL BARU ROLE GURU MAPEL & WALI KELAS ===

data class Kelas(
    val id: String = "",
    val name: String = "",
    val homeroomTeacherId: String = "",
    val schoolLevel: String = "SMP",
    val grade: Int = 9
)

data class Student(
    val id: String = "",
    val name: String = "",
    val nis: String = "",
    val classId: String = ""
)

data class Kompetensi(
    val id: String = "",
    val subjectId: String = "",
    val code: String = "",
    val name: String = ""
)

data class Periode(
    val id: String = "",
    val name: String = "",
    val semester: String = "Ganjil",
    val academicYear: String = "2025/2026"
)

data class Penugasan(
    val id: String = "",
    val teacherId: String = "",
    val subjectId: String = "",
    val classId: String = ""
)

enum class ScoreType(val label: String) {
    KUIS_HARIAN("Kuis Harian"),
    KARAKTER("Penilaian Karakter")
}

data class Score(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val teacherId: String = "",
    val subjectId: String = "",
    val classId: String = "",
    val competenceId: String = "",
    val periodId: String = "",
    val scoreType: String = "KUIS_HARIAN",
    val scoreValue: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AiInsight(
    val id: String = "",
    val studentId: String = "",
    val classId: String = "",
    val subjectId: String = "",
    val averageScore: Double = 0.0,
    val lowestScore: Double = 0.0,
    val highestScore: Double = 0.0,
    val recommendation: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
