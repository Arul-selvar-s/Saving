package com.saving.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SavingsGreen,
    secondary = FlowerCenter,
    background = SoftPeach,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark
)

private val DarkColors = darkColorScheme(
    primary = SavingsGreen,
    secondary = FlowerCenter,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF262529)
)

@Composable
fun SavingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
