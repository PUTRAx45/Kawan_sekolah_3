package com.example.kawansekolah.domain

data class StatisticsSummary(
    val count: Int,
    val average: Double,
    val highest: Double,
    val lowest: Double
)

object StatisticsCalculator {

    fun calculateAverage(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        val sum = scores.sum()
        return sum / scores.size
    }

    fun calculateHighest(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        return scores.maxOrNull() ?: 0.0
    }

    fun calculateLowest(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        return scores.minOrNull() ?: 0.0
    }

    fun calculateSummary(scores: List<Double>): StatisticsSummary {
        if (scores.isEmpty()) {
            return StatisticsSummary(
                count = 0,
                average = 0.0,
                highest = 0.0,
                lowest = 0.0
            )
        }
        return StatisticsSummary(
            count = scores.size,
            average = calculateAverage(scores),
            highest = calculateHighest(scores),
            lowest = calculateLowest(scores)
        )
    }
}
