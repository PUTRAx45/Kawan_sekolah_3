package com.example.kawansekolah.data.model

enum class SchoolLevel(val label: String) {
    SD("Tingkat SD"),
    SMP("Tingkat SMP"),
    SMA("Tingkat SMA")
}

data class GradeItem(
    val id: Int,
    val subjectName: String,
    val code: String,
    val category: String,
    val credits: Int,
    val score: Double,
    val semester: Int = 1,
    val level: SchoolLevel = SchoolLevel.SMP,
    val conductStatus: String = "Rajin & Aktif",
    val teacherNotes: String = "Siswa menunjukkan pemahaman yang baik dan aktif berpartisipasi.",
    val hadirCount: Int = 18,
    val izinCount: Int = 1,
    val sakitCount: Int = 0,
    val alpaCount: Int = 0
) {
    val predicate: String
        get() = when {
            score >= 88.0 -> "A"
            score >= 80.0 -> "B+"
            score >= 75.0 -> "B"
            score >= 70.0 -> "C+"
            score >= 60.0 -> "C"
            else -> "D"
        }
}
