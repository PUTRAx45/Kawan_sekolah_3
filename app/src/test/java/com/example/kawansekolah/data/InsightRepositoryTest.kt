package com.example.kawansekolah.data

import com.example.kawansekolah.data.model.AiInsightData
import com.example.kawansekolah.data.model.AiInsightResponse
import com.example.kawansekolah.domain.AiResponseValidator
import org.junit.Assert.*
import org.junit.Test

class InsightRepositoryTest {

    @Test
    fun `verifyAiResponseStructure flags invalid response status or empty data via validator`() {
        val invalidStatusResponse = AiInsightResponse(
            status = "error",
            message = "Server error",
            data = null
        )
        val (valid1, _) = AiResponseValidator.validateResponse(invalidStatusResponse)
        assertFalse(valid1)

        val emptyRecommendationResponse = AiInsightResponse(
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
        val (valid2, _) = AiResponseValidator.validateResponse(emptyRecommendationResponse)
        assertFalse(valid2)
    }
}
