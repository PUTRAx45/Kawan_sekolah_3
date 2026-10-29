package com.example.kawansekolah.ui.tugas

import androidx.compose.runtime.Composable
import com.example.kawansekolah.data.model.Task
import com.example.kawansekolah.data.repository.SchoolRepository
import com.example.kawansekolah.ui.nilai.NilaiScreen

@Composable
fun TugasScreen(
    userName: String,
    tasks: List<Task>,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onToggleTask: (Int) -> Unit
) {
    NilaiScreen(
        userName = userName,
        grades = SchoolRepository.getInitialGrades(),
        unreadCount = unreadCount,
        onNotificationClick = onNotificationClick
    )
}
