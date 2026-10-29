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
            throw IllegalStateException("⚠️ Fitur AI memerlukan URL Backend Online. Silakan isi URL server backend Anda di 'app/build.gradle.kts' (menggantikan placeholder 'GANTI-DENGAN-URL-BACKEND').")
        }

        val startTime = System.currentTimeMillis()
        var httpCode = 200
        val targetUrl = DevHost.getBackendBaseUrl().trimEnd('/') + "/api/v1/ai-analytics/insight"

        val currentUser = Firebase.auth.currentUser
        val hasToken = currentUser != null
        val tokenStatus = if (hasToken) "ada" else "tidak ada"

        if (BuildConfig.DEBUG) {
            Log.d("AiChat", "[REQUEST START] URL: $targetUrl | Mode: ${request.mode} | Mapel: ${request.subject} | Token: $tokenStatus | Question: \"${request.question ?: ""}\"")
            Log.d("Perf", "[PERF START] Endpoint: $targetUrl | Token: $tokenStatus")
        }

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

            if (BuildConfig.DEBUG) {
                Log.d("AiChat", "[RESPONSE SUCCESS] URL: $targetUrl | Status: ${response.status} | Code: $httpCode | Duration: ${duration}ms | Token: $tokenStatus | ReplyLen: ${response.data?.aiRecommendation?.length ?: 0}")
                Log.d("Perf", "[PERF SUCCESS] Endpoint: $targetUrl | Duration: ${duration}ms | Code: $httpCode | Token: $tokenStatus")
            }

            if (response.data?.aiRecommendation.isNullOrBlank()) {
                throw IllegalStateException("Jawaban AI kosong, silakan ulangi pertanyaan Anda.")
            }

            return response

        } catch (e: HttpException) {
            httpCode = e.code()
            val duration = System.currentTimeMillis() - startTime

            if (BuildConfig.DEBUG) {
                Log.e("AiChat", "[RESPONSE ERROR HTTP] URL: $targetUrl | Code: $httpCode | Exception: ${e.javaClass.simpleName} (${e.message()}) | Duration: ${duration}ms | Token: $tokenStatus")
                Log.e("Perf", "[PERF ERROR] Endpoint: $targetUrl | Duration: ${duration}ms | Code: $httpCode | Exception: ${e.javaClass.simpleName} (${e.message()}) | Token: $tokenStatus")
            }

            val friendlyMsg = when (httpCode) {
                401 -> "Aktivitas login berakhir atau autentikasi tidak valid (401). Silakan login ulang."
                403 -> "Akses ditolak (403). Akun Anda tidak memiliki izin untuk menggunakan fitur ini."
                402 -> "Kuota AI Analytics telah habis (402). Silakan periksa akun OpenRouter Anda."
                429 -> "Permintaan AI terlalu padat (429). Silakan tunggu sebentar dan coba lagi."
                else -> "Gagal memproses data AI pada server (HTTP $httpCode)."
            }
            throw IllegalStateException(friendlyMsg)

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime

            if (BuildConfig.DEBUG) {
                Log.e("AiChat", "[RESPONSE ERROR EXCEPTION] URL: $targetUrl | Exception: ${e.javaClass.simpleName} (${e.message}) | Duration: ${duration}ms | Token: $tokenStatus")
                Log.e("Perf", "[PERF ERROR] Endpoint: $targetUrl | Duration: ${duration}ms | Exception: ${e.javaClass.simpleName} (${e.message}) | Token: $tokenStatus")
            }

            val friendlyMsg = when (e) {
                is ConnectException, is UnknownHostException ->
                    "Gagal terhubung ke server AI di '$targetUrl'. Pastikan server backend Express Node.js sedang berjalan."
                is SocketTimeoutException ->
                    "Koneksi ke server AI melebihi batas waktu (timeout)."
                is IllegalStateException -> e.message ?: "Terjadi kendala pada layanan AI."
                else -> "Terjadi kendala koneksi saat menghubungi AI Analytics. Pastikan backend server aktif."
            }
            throw IllegalStateException(friendlyMsg)
        }
    }
}
