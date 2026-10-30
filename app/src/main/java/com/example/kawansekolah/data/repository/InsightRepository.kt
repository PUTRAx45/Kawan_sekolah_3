package com.example.kawansekolah.data.repository

import com.example.kawansekolah.data.model.AiInsight
import com.example.kawansekolah.data.model.AiInsightRequest
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.model.TopicScoreDto
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.AiResponseValidator
import com.example.kawansekolah.domain.StatisticsCalculator
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.net.ConnectException
import java.net.SocketTimeoutException

object InsightRepository {

    suspend fun generateClassInsight(
        userRole: String,
        userId: String,
        classHomeroomTeacherId: String,
        assignedTeacherId: String,
        studentId: String,
        classId: String,
        subjectId: String,
        subjectName: String,
        scores: List<Score>
    ): Result<AiInsight> {
        if (!AccessPolicy.canViewClassScores(userRole, userId, classHomeroomTeacherId, assignedTeacherId)) {
            return Result.failure(
                IllegalStateException("Akses ditolak: Anda tidak memiliki wewenang melihat rekapitulasi/insight kelas ini.")
            )
        }

        val kuisScores = scores.filter { it.scoreType == "KUIS_HARIAN" }.map { it.scoreValue }
        val karakterScores = scores.filter { it.scoreType == "KARAKTER" }.map { it.scoreValue }

        val avgKuis = StatisticsCalculator.calculateAverage(kuisScores)
        val avgKarakter = StatisticsCalculator.calculateAverage(karakterScores)
        val allScoreNumbers = scores.map { it.scoreValue }
        val overallStats = StatisticsCalculator.calculateSummary(allScoreNumbers)

        val req = AiInsightRequest(
            mode = "AUTO_INSIGHT",
            studentId = studentId,
            studentName = "Siswa Kelas $classId",
            schoolLevel = "SMP",
            grade = 9,
            subject = subjectName,
            finalScore = overallStats.average,
            topics = listOf(
                TopicScoreDto("Kuis Harian (Rata-rata)", avgKuis, 1.0),
                TopicScoreDto("Penilaian Karakter (Rata-rata)", avgKarakter, 1.0)
            )
        )

        return try {
            withTimeout(8_000L) {
                val res = AiRepository.fetchAiInsight(req)

                val (isValid, errorMsg) = AiResponseValidator.validateResponse(res)
                if (!isValid) {
                    return@withTimeout Result.failure<AiInsight>(
                        IllegalStateException("Struktur respons AI tidak valid: $errorMsg")
                    )
                }

                val recommendation = res.data?.aiRecommendation ?: ""

                val insight = AiInsight(
                    id = "ins_${System.currentTimeMillis()}",
                    studentId = studentId,
                    classId = classId,
                    subjectId = subjectId,
                    averageScore = overallStats.average,
                    lowestScore = overallStats.lowest,
                    highestScore = overallStats.highest,
                    recommendation = recommendation,
                    createdAt = System.currentTimeMillis()
                )
                Result.success(insight)
            }
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is TimeoutCancellationException, is SocketTimeoutException ->
                    "Koneksi ke layanan AI Analytics mengalami timeout (8 detik)."
                is ConnectException ->
                    "Gagal terhubung ke server backend AI Analytics. Pastikan server aktif."
                is IllegalStateException -> e.message ?: "Respons AI tidak valid."
                else -> e.message ?: "Terjadi kendala saat menghubungi layanan AI Analytics."
            }

            Result.failure(IllegalStateException(errorMsg, e))
        }
    }
}
