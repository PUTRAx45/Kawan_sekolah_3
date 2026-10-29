package com.example.kawansekolah.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
