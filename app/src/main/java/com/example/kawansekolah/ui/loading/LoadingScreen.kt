package com.example.kawansekolah.ui.loading

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kawansekolah.R
import com.example.kawansekolah.ui.loading.components.LoadingProgressBar

@Composable
fun LoadingScreen(
    onLoadingFinished: () -> Unit
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
        onLoadingFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fullscreen Background Image
        Image(
            painter = painterResource(id = R.drawable.splash_background1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Center Content: Custom Loading Progress Bar positioned below the AI text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 398.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingProgressBar(
                progress = animProgress.value
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreen(onLoadingFinished = {})
}
