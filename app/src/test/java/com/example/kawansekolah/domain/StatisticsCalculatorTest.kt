package com.example.kawansekolah.domain

import org.junit.Assert.*
import org.junit.Test

class StatisticsCalculatorTest {

    @Test
    fun `calculateSummary handles empty list safely`() {
        val emptyList = emptyList<Double>()
        val stats = StatisticsCalculator.calculateSummary(emptyList)

        assertEquals(0, stats.count)
        assertEquals(0.0, stats.average, 0.001)
        assertEquals(0.0, stats.highest, 0.001)
        assertEquals(0.0, stats.lowest, 0.001)
    }

    @Test
    fun `calculateSummary computes correct average highest and lowest`() {
        val scores = listOf(80.0, 90.0, 100.0, 70.0)
        val stats = StatisticsCalculator.calculateSummary(scores)

        assertEquals(4, stats.count)
        assertEquals(85.0, stats.average, 0.001)
        assertEquals(100.0, stats.highest, 0.001)
        assertEquals(70.0, stats.lowest, 0.001)
    }
}
