package com.example.kawansekolah.ui.loading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp,
    trackColor: Color = Color.White.copy(alpha = 0.75f),
    progressColor: Color = Color(0xFF0862FA)
) {
    Box(
        modifier = modifier
            .width(width)
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp))
                .background(progressColor)
        )
    }
}
