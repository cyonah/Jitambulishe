package com.jitambulishe.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E6F5C),
    onPrimary = Color.White,
    secondary = Color(0xFF2E7D8A),
    background = Color(0xFFF7F9F8),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6DD6B5),
    onPrimary = Color.Black,
    secondary = Color(0xFF7AC6D1),
    background = Color(0xFF101413),
    surface = Color(0xFF1A1F1E)
)

@Composable
fun JitambulisheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
