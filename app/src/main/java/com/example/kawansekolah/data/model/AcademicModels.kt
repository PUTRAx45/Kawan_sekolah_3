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
