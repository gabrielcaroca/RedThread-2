package com.example.redthread.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    background = Black,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    secondary = CardGray
)

@Composable
fun RedThreadTheme(content: @Composable () -> Unit) {
    val scheme = DarkScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content
    )
}
