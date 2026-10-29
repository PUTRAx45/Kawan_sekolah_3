package com.example.kawansekolah.data.model

data class TeacherProfile(
    val teacherId: String = "",
    val fullName: String = "",
    val schoolName: String = "",
    val schoolLevel: String = "SMP", // SD, SMP, SMA
    val classesTaught: List<String> = emptyList(), // e.g. ["9-A", "9-B"]
    val profileComplete: Boolean = false
)
