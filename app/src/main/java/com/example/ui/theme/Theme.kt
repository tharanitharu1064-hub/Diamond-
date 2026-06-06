package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Indigo200,
    onPrimary = Slate900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo50,
    secondary = Slate200,
    onSecondary = DarkSlateBg,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Indigo50,
    background = DarkSlateBg,
    onBackground = Indigo50,
    surface = DarkSurface,
    onSurface = Indigo50,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    outlineVariant = Indigo700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo700,
    secondary = Slate800,
    onSecondary = White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    background = Neutral50,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    outlineVariant = Indigo100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce the "Vibrant Palette" brand experience
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
