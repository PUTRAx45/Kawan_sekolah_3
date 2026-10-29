package com.example.kawansekolah.utils

import com.example.kawansekolah.BuildConfig
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

data class ServiceCheckResult(
    val name: String,
    val endpoint: String,
    val isOk: Boolean,
    val latencyMs: Long,
    val details: String
)

object ConnectionDiagnostics {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun checkAllServices(): List<ServiceCheckResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ServiceCheckResult>()
        val host = DevHost.getHost()

        // Line Diagnostik Project ID Firebase, Mode, dan BASE_URL
        val activeProjectId = try {
            FirebaseApp.getInstance().options.projectId ?: "Unknown"
        } catch (_: Exception) {
            "Uninitialized"
        }
        val firebaseMode = if (BuildConfig.USE_EMULATOR) "EMULATOR" else "CLOUD"
        val activeBaseUrl = DevHost.getBackendBaseUrl()

        results.add(
            ServiceCheckResult(
                name = "Info Lingkungan & Firebase",
                endpoint = "Project ID: $activeProjectId",
                isOk = true,
                latencyMs = 0,
                details = "Mode: $firebaseMode | BASE_URL: $activeBaseUrl"
            )
        )

        if (BuildConfig.USE_EMULATOR) {
            // 1. Auth Emulator
            val authResult = checkTcpPort("Auth Emulator", host, 9099)
            results.add(authResult)

            // 2. Firestore Emulator
            val firestoreResult = checkTcpPort("Firestore Emulator", host, 8080)
            results.add(firestoreResult)
        } else {
            results.add(
                ServiceCheckResult(
                    name = "Firebase Cloud ($firebaseMode)",
                    endpoint = "Project ID: $activeProjectId",
                    isOk = true,
                    latencyMs = 0,
                    details = "Koneksi Langsung ke Firebase Cloud ($activeProjectId)"
                )
            )
        }

        // 3. Backend AI
        if (DevHost.isPlaceholderBaseUrl()) {
            results.add(
                ServiceCheckResult(
                    name = "Backend AI Scolarsia",
                    endpoint = activeBaseUrl,
                    isOk = false,
                    latencyMs = 0,
                    details = "⚠️ BASE_URL belum diisi (masih placeholder 'GANTI-DENGAN-URL-BACKEND' pada app/build.gradle.kts)"
                )
            )
        } else {
            val healthUrl = activeBaseUrl.trimEnd('/') + "/health"
            val backendResult = checkHttpEndpoint("Backend AI Scolarsia", healthUrl)
            results.add(backendResult)
        }

        results
    }

    private fun checkTcpPort(name: String, primaryHost: String, port: Int): ServiceCheckResult {
        val hostsToTry = if (!DevHost.isPhysicalDevice() && (primaryHost == "10.0.2.2" || primaryHost == "127.0.0.1")) {
            listOf(primaryHost, if (primaryHost == "10.0.2.2") "127.0.0.1" else "10.0.2.2")
        } else {
            listOf(primaryHost)
        }

        var isOk = false
        var details = ""
        var successHost = primaryHost
        var time = 0L

        for (host in hostsToTry) {
            time = measureTimeMillis {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), 3000)
                        isOk = true
                        successHost = host
                        details = "Port $port terbuka dan merespons ($host)"
                    }
                } catch (e: Exception) {
                    isOk = false
                    details = "Koneksi ditolak / timeout (${e.message})"
                }
            }
            if (isOk) break
        }
        return ServiceCheckResult(name, "$successHost:$port", isOk, time, details)
    }

    private fun checkHttpEndpoint(name: String, primaryUrl: String): ServiceCheckResult {
        val urlsToTry = if (!DevHost.isPhysicalDevice() && (primaryUrl.contains("10.0.2.2") || primaryUrl.contains("127.0.0.1"))) {
            if (primaryUrl.contains("10.0.2.2")) {
                listOf(primaryUrl, primaryUrl.replace("10.0.2.2", "127.0.0.1"))
            } else {
                listOf(primaryUrl, primaryUrl.replace("127.0.0.1", "10.0.2.2"))
            }
        } else {
            listOf(primaryUrl)
        }

        var isOk = false
        var details = ""
        var successUrl = primaryUrl
        var time = 0L

        for (url in urlsToTry) {
            time = measureTimeMillis {
                try {
                    val req = Request.Builder().url(url).build()
                    client.newCall(req).execute().use { response ->
                        if (response.isSuccessful) {
                            isOk = true
                            successUrl = url
                            details = "HTTP 200 OK (${response.body?.string()?.take(50) ?: ""})"
                        } else {
                            isOk = false
                            details = "HTTP ${response.code}"
                        }
                    }
                } catch (e: Exception) {
                    isOk = false
                    details = "Gagal terhubung (${e.message})"
                }
            }
            if (isOk) break
        }
        return ServiceCheckResult(name, successUrl, isOk, time, details)
    }

    fun getDiagnosticHint(): String {
        return DevHost.getDiagnosticsWarningMessage()
    }
}
