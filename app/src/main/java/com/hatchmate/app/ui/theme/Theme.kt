package com.hatchmate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6D28D9),
    secondary = Color(0xFF7C3AED),
    tertiary = Color(0xFF06B6D4)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    secondary = Color(0xFF06B6D4),
    tertiary = Color(0xFF06B6D4)
)

@Composable
fun HatchMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
