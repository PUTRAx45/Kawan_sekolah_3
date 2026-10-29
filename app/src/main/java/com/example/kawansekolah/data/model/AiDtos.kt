package com.example.kawansekolah.data.model

data class TopicScoreDto(
    val name: String,
    val score: Double,
    val weight: Double
)

data class ChatHistoryDto(
    val role: String, // "user" or "assistant"
    val content: String
)

data class AiInsightRequest(
    val mode: String = "AUTO_INSIGHT", // "AUTO_INSIGHT" or "CHAT"
    val studentId: String = "",
    val studentName: String = "",
    val userRole: String = "SISWA",
    val schoolLevel: String = "SMP",
    val grade: Int = 9,
    val subject: String = "",
    val finalScore: Double = 0.0,
    val topics: List<TopicScoreDto> = emptyList(),
    val question: String? = null,
    val history: List<ChatHistoryDto> = emptyList()
)

data class AiInsightData(
    val studentName: String? = null,
    val subject: String? = null,
    val className: String? = null,
    val highestTopic: String? = null,
    val lowestTopic: String? = null,
    val aiRecommendation: String? = null
)

data class AiInsightResponse(
    val status: String = "success",
    val message: String = "",
    val data: AiInsightData? = null
)
