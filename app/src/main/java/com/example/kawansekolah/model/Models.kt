package com.example.kawansekolah.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Module(
    val id: Int,
    val title: String,
    val duration: String,
    val isCompleted: Boolean
)

data class Subject(
    val name: String,
    val category: String,
    val modules: List<Module>,
    val icon: ImageVector,
    val color: Color
) {
    val modulesCount: Int get() = modules.size
    val completedModulesCount: Int get() = modules.count { it.isCompleted }
    val progress: Float get() = if (modulesCount > 0) completedModulesCount.toFloat() / modulesCount else 0f
}

data class Task(
    val id: Int,
    val title: String,
    val subject: String,
    val deadline: String,
    val isChecked: Boolean
)

data class ChatMessage(
    val id: Int,
    val isUser: Boolean,
    val text: String,
    val timestamp: String
)
