package com.example.kawansekolah.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = ColorWhite,
    primaryContainer = PrimarySoftBlue,
    onPrimaryContainer = PrimaryBlue,
    secondary = PrimaryBlue,
    onSecondary = ColorWhite,
    tertiary = NavyContainer,
    onTertiary = ColorWhite,
    background = BackgroundMuted,
    onBackground = DarkText,
    surface = ColorWhite,
    onSurface = DarkText,
    surfaceVariant = BackgroundMuted,
    onSurfaceVariant = TextMuted,
    outline = CardBorderStroke,
    error = DangerRed,
    onError = ColorWhite,
    errorContainer = DangerRedBg,
    onErrorContainer = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = ColorWhite,
    primaryContainer = Color(0xFF0F172A),
    onPrimaryContainer = ColorWhite,
    secondary = Color(0xFF3B82F6),
    onSecondary = ColorWhite,
    tertiary = Color(0xFF1E3A8A),
    onTertiary = ColorWhite,
    background = Color(0xFF0F172A), // Dark Background #0F172A
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF161F30), // Dark Surface #161F30
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1E293B), // Dark Surface Variant #1E293B
    onSurfaceVariant = Color(0xFF9CA3AF), // Light Gray #9CA3AF
    outline = Color(0xFF334155),
    error = DangerRed,
    onError = ColorWhite,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun ScolarsiaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
