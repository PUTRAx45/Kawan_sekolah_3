package com.example.kawansekolah.domain

data class CsvParseResult(
    val lineNumber: Int,
    val studentId: String,
    val studentName: String,
    val score: Double,
    val scoreType: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object ScoreValidator {

    fun validateScore(score: Double): Boolean {
        return score in 0.0..100.0
    }

    fun parseScoreInput(input: String): Pair<Boolean, Double?> {
        val score = input.trim().toDoubleOrNull()
        return if (score != null && validateScore(score)) {
            Pair(true, score)
        } else {
            Pair(false, null)
        }
    }

    fun parseCsvRow(lineNumber: Int, line: String): CsvParseResult {
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("#")) {
            return CsvParseResult(
                lineNumber = lineNumber,
                studentId = "",
                studentName = "",
                score = 0.0,
                scoreType = "",
                isValid = false,
                errorMessage = "Baris kosong atau komentar"
            )
        }

        val tokens = trimmed.split(",").map { it.trim() }
        if (tokens.size < 4) {
            return CsvParseResult(
                lineNumber = lineNumber,
                studentId = tokens.getOrElse(0) { "" },
                studentName = tokens.getOrElse(1) { "" },
                score = 0.0,
                scoreType = tokens.getOrElse(3) { "" },
                isValid = false,
                errorMessage = "Format kolom tidak sesuai. Wajib: studentId,nama,nilai,jenisScore"
            )
        }

        val studentId = tokens[0]
        val studentName = tokens[1]
        val rawScore = tokens[2]
        val scoreType = tokens[3].uppercase()

        if (studentId.isBlank()) {
            return CsvParseResult(
                lineNumber = lineNumber,
                studentId = "",
                studentName = studentName,
                score = 0.0,
                scoreType = scoreType,
                isValid = false,
                errorMessage = "ID Siswa tidak boleh kosong"
            )
        }

        val scoreVal = rawScore.toDoubleOrNull()
        if (scoreVal == null || !validateScore(scoreVal)) {
            return CsvParseResult(
                lineNumber = lineNumber,
                studentId = studentId,
                studentName = studentName,
                score = scoreVal ?: 0.0,
                scoreType = scoreType,
                isValid = false,
                errorMessage = "Nilai '$rawScore' tidak valid (harus angka 0.0 - 100.0)"
            )
        }

        if (scoreType != "KUIS_HARIAN" && scoreType != "KARAKTER") {
            return CsvParseResult(
                lineNumber = lineNumber,
                studentId = studentId,
                studentName = studentName,
                score = scoreVal,
                scoreType = scoreType,
                isValid = false,
                errorMessage = "Jenis nilai '$scoreType' tidak valid. Gunakan KUIS_HARIAN atau KARAKTER"
            )
        }

        return CsvParseResult(
            lineNumber = lineNumber,
            studentId = studentId,
            studentName = studentName,
            score = scoreVal,
            scoreType = scoreType,
            isValid = true,
            errorMessage = null
        )
    }

    fun validateCsvContent(csvContent: String): List<CsvParseResult> {
        val lines = csvContent.lines()
        val results = mutableListOf<CsvParseResult>()

        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            if (line.trim().isNotBlank() && !line.startsWith("studentId", ignoreCase = true)) {
                results.add(parseCsvRow(lineNumber, line))
            }
        }
        return results
    }
}
