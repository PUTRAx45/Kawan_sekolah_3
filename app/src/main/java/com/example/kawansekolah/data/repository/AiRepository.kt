package com.example.kawansekolah.data.repository

import android.util.Log
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.data.model.AiInsightRequest
import com.example.kawansekolah.data.model.AiInsightResponse
import com.example.kawansekolah.data.network.ApiService
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.system.measureTimeMillis

object AiRepository {

    private val apiService: ApiService by lazy { ApiService.create() }

    suspend fun fetchAiInsight(request: AiInsightRequest): AiInsightResponse {
        if (!BuildConfig.USE_EMULATOR && DevHost.isPlaceholderBaseUrl()) {
            throw IllegalStateException("⚠️ Fitur AI memerlukan URL Backend Online. Silakan isi URL server backend Anda di 'app/build.gradle.kts'.")
        }

        val startTime = System.currentTimeMillis()
        var httpCode = 200
        val targetUrl = DevHost.getBackendBaseUrl().trimEnd('/') + "/api/v1/ai-analytics/insight"

        val currentUser = Firebase.auth.currentUser

        try {
            var response: AiInsightResponse
            val duration = measureTimeMillis {
                val idToken = try {
                    currentUser?.getIdToken(false)?.await()?.token ?: "mock-token-debug"
                } catch (_: Exception) {
                    "mock-token-debug"
                }
                val authHeader = "Bearer $idToken"

                response = apiService.getAiInsight(authHeader, request)
            }

            val replyText = response.data?.aiRecommendation ?: response.message ?: ""
            val snippet = replyText.take(200).replace("\n", " ")

            Log.i("ScolarsiaNet", "🤖 AI Request -> URL: $targetUrl | Status HTTP: $httpCode | Duration: ${duration}ms | Reply (200 char): \"$snippet\"")

            if (replyText.isBlank()) {
                throw IllegalStateException("Jawaban AI kosong, silakan ulangi pertanyaan Anda.")
            }

            return response

        } catch (e: HttpException) {
            httpCode = e.code()
            val duration = System.currentTimeMillis() - startTime
            val errorBody = try { e.response()?.errorBody()?.string() ?: "" } catch (_: Exception) { "" }

            Log.e("ScolarsiaNet", "❌ AI Request Error -> URL: $targetUrl | Status HTTP: $httpCode | Duration: ${duration}ms | Response: $errorBody")

            val friendlyMsg = when (httpCode) {
                401 -> "Aktivitas login berakhir atau autentikasi tidak valid (HTTP 401). Silakan login ulang."
                403 -> "Akses ditolak (HTTP 403). Akun Anda tidak memiliki izin untuk mengakses data ini."
                402 -> "Kuota AI Analytics telah habis (HTTP 402). Silakan periksa akun OpenRouter Anda."
                429 -> "Permintaan AI terlalu padat (HTTP 429). Silakan tunggu sebentar dan coba lagi."
                500 -> if (errorBody.contains("belum dikonfigurasi")) "AI belum dikonfigurasi pada file .env server backend." else "Gagal memproses data AI pada server (HTTP 500)."
                else -> "Gagal memproses data AI pada server (HTTP $httpCode)."
            }
            throw IllegalStateException(friendlyMsg)

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.e("ScolarsiaNet", "❌ AI Request Exception -> URL: $targetUrl | Exception: ${e.javaClass.simpleName} (${e.message}) | Duration: ${duration}ms")

            val friendlyMsg = when (e) {
                is ConnectException, is UnknownHostException ->
                    "Gagal terhubung ke server AI Backend di '$targetUrl'. Pastikan server backend Node.js Express aktif dan laptop menyala."
                is SocketTimeoutException ->
                    "Koneksi ke server AI melebihi batas waktu 30 detik (Timeout)."
                is IllegalStateException -> e.message ?: "Terjadi kendala pada layanan AI."
                else -> "Terjadi kendala koneksi saat menghubungi AI Analytics. Pastikan backend server aktif."
            }
            throw IllegalStateException(friendlyMsg)
        }
    }
}
