package com.example.kawansekolah.domain

import com.example.kawansekolah.data.model.AiInsightResponse

object AiResponseValidator {

    fun validateResponse(response: AiInsightResponse?): Pair<Boolean, String> {
        if (response == null) {
            return Pair(false, "Respons AI kosong (null).")
        }

        if (response.status != "success") {
            return Pair(false, "Respons AI gagal dengan status '${response.status}'. Pesan: ${response.message}")
        }

        val data = response.data
        if (data == null) {
            return Pair(false, "Data respons AI tidak ditemukan (null).")
        }

        val recommendation = data.aiRecommendation
        if (recommendation.isNullOrBlank()) {
            return Pair(false, "Teks rekomendasi AI kosong.")
        }

        return Pair(true, "Valid")
    }
}
