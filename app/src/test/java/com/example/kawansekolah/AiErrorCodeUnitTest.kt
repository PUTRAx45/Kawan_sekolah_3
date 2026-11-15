package com.example.kawansekolah

import org.junit.Assert.assertEquals
import org.junit.Test

class AiErrorCodeUnitTest {

    private fun mapAiErrorCodeToMessage(code: String, httpCode: Int): String {
        return when (code) {
            "AI_KEY_INVALID" -> "AI belum dikonfigurasi atau kunci tidak valid"
            "AI_QUOTA" -> "Kuota AI Analytics telah habis (HTTP 502). Silakan periksa akun OpenRouter Anda."
            "AI_RATE_LIMIT" -> "Permintaan AI terlalu padat. Silakan tunggu sebentar dan coba lagi."
            "AI_UNAVAILABLE" -> "Layanan AI saat ini tidak dapat dijangkau. Silakan coba beberapa saat lagi."
            else -> when (httpCode) {
                401 -> "Aktivitas login berakhir atau autentikasi tidak valid (HTTP 401). Silakan login ulang."
                403 -> "Akses ditolak (HTTP 403). Akun Anda tidak memiliki izin untuk mengakses data ini."
                402 -> "Kuota AI Analytics telah habis (HTTP 502). Silakan periksa akun OpenRouter Anda."
                429 -> "Permintaan AI terlalu padat (HTTP 429). Silakan tunggu sebentar dan coba lagi."
                502 -> "AI belum dikonfigurasi atau kunci tidak valid"
                else -> "Gagal memproses data AI pada server (HTTP $httpCode)."
            }
        }
    }

    @Test
    fun `1 test mapping AI_KEY_INVALID`() {
        val msg = mapAiErrorCodeToMessage("AI_KEY_INVALID", 502)
        assertEquals("AI belum dikonfigurasi atau kunci tidak valid", msg)
    }

    @Test
    fun `2 test mapping AI_QUOTA`() {
        val msg = mapAiErrorCodeToMessage("AI_QUOTA", 502)
        assertEquals("Kuota AI Analytics telah habis (HTTP 502). Silakan periksa akun OpenRouter Anda.", msg)
    }

    @Test
    fun `3 test mapping AI_RATE_LIMIT`() {
        val msg = mapAiErrorCodeToMessage("AI_RATE_LIMIT", 503)
        assertEquals("Permintaan AI terlalu padat. Silakan tunggu sebentar dan coba lagi.", msg)
    }

    @Test
    fun `4 test mapping AI_UNAVAILABLE`() {
        val msg = mapAiErrorCodeToMessage("AI_UNAVAILABLE", 503)
        assertEquals("Layanan AI saat ini tidak dapat dijangkau. Silakan coba beberapa saat lagi.", msg)
    }
}
