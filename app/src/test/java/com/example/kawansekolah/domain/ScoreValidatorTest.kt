package com.example.kawansekolah.domain

import org.junit.Assert.*
import org.junit.Test

class ScoreValidatorTest {

    @Test
    fun `validateScore returns true for valid score range`() {
        assertTrue(ScoreValidator.validateScore(0.0))
        assertTrue(ScoreValidator.validateScore(50.5))
        assertTrue(ScoreValidator.validateScore(100.0))
    }

    @Test
    fun `validateScore returns false for out of range score`() {
        assertFalse(ScoreValidator.validateScore(-1.0))
        assertFalse(ScoreValidator.validateScore(100.1))
        assertFalse(ScoreValidator.validateScore(150.0))
    }

    @Test
    fun `parseScoreInput parses valid and invalid score string`() {
        val (valid1, score1) = ScoreValidator.parseScoreInput("85.5")
        assertTrue(valid1)
        assertEquals(85.5, score1!!, 0.001)

        val (valid2, score2) = ScoreValidator.parseScoreInput("120.0")
        assertFalse(valid2)
        assertNull(score2)

        val (valid3, score3) = ScoreValidator.parseScoreInput("abc")
        assertFalse(valid3)
        assertNull(score3)
    }

    @Test
    fun `parseCsvRow validates valid CSV line correctly`() {
        val line = "std_1, Ahmad Budi, 88.5, KUIS_HARIAN"
        val result = ScoreValidator.parseCsvRow(1, line)

        assertTrue(result.isValid)
        assertEquals("std_1", result.studentId)
        assertEquals("Ahmad Budi", result.studentName)
        assertEquals(88.5, result.score, 0.001)
        assertEquals("KUIS_HARIAN", result.scoreType)
        assertNull(result.errorMessage)
    }

    @Test
    fun `parseCsvRow flags invalid score or format`() {
        val lineInvalidScore = "std_1, Ahmad Budi, 120.0, KUIS_HARIAN"
        val res1 = ScoreValidator.parseCsvRow(1, lineInvalidScore)

        assertFalse(res1.isValid)
        assertNotNull(res1.errorMessage)

        val lineInvalidType = "std_1, Ahmad Budi, 85.0, TYPE_SALAH"
        val res2 = ScoreValidator.parseCsvRow(2, lineInvalidType)

        assertFalse(res2.isValid)
        assertNotNull(res2.errorMessage)
    }
}
