package com.example.kawansekolah.ui.splash

import androidx.compose.runtime.Composable
import com.example.kawansekolah.ui.loading.LoadingScreen

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LoadingScreen(onLoadingFinished = onSplashFinished)
}
