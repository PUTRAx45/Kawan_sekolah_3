package com.example.kawansekolah.domain

import com.example.kawansekolah.data.model.AiInsightData
import com.example.kawansekolah.data.model.AiInsightResponse
import org.junit.Assert.*
import org.junit.Test

class AiResponseValidatorTest {

    @Test
    fun `validateResponse flags error status`() {
        val response = AiInsightResponse(
            status = "error",
            message = "Internal Server Error",
            data = null
        )
        val (isValid, msg) = AiResponseValidator.validateResponse(response)

        assertFalse(isValid)
        assertTrue(msg.contains("status 'error'"))
    }

    @Test
    fun `validateResponse flags null data`() {
        val response = AiInsightResponse(
            status = "success",
            message = "OK",
            data = null
        )
        val (isValid, msg) = AiResponseValidator.validateResponse(response)

        assertFalse(isValid)
        assertTrue(msg.contains("null"))
    }

    @Test
    fun `validateResponse flags empty recommendation`() {
        val response = AiInsightResponse(
            status = "success",
            message = "OK",
            data = AiInsightData(
                studentName = "Ahmad Budi",
                subject = "Matematika",
                className = "9-A",
                highestTopic = "Aritmatika",
                lowestTopic = "Pitagoras",
                aiRecommendation = ""
            )
        )
        val (isValid, msg) = AiResponseValidator.validateResponse(response)

        assertFalse(isValid)
        assertTrue(msg.contains("kosong"))
    }

    @Test
    fun `validateResponse returns valid for complete response`() {
        val response = AiInsightResponse(
            status = "success",
            message = "OK",
            data = AiInsightData(
                studentName = "Ahmad Budi",
                subject = "Matematika",
                className = "9-A",
                highestTopic = "Aritmatika",
                lowestTopic = "Pitagoras",
                aiRecommendation = "Siswa Ahmad Budi berprestasi sangat baik."
            )
        )
        val (isValid, msg) = AiResponseValidator.validateResponse(response)

        assertTrue(isValid)
        assertEquals("Valid", msg)
    }
}
