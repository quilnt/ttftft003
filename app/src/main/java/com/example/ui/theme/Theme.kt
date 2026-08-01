package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HextechDarkColorScheme = darkColorScheme(
    primary = TftGold,
    onPrimary = VoidNavy,
    primaryContainer = TftGoldDark,
    onPrimaryContainer = TftGoldLight,
    secondary = HextechCyan,
    onSecondary = VoidNavy,
    tertiary = AccentPurple,
    background = DarkBackground,
    onBackground = Color.White,
    surface = CardSurface,
    onSurface = Color.White,
    surfaceVariant = CardSurfaceBorder,
    onSurfaceVariant = TextMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HextechDarkColorScheme,
        typography = Typography,
        content = content
    )
}

