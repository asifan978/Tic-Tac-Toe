package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DreamyColorScheme = lightColorScheme(
    primary = PastelPinkPrimary,
    onPrimary = PastelWhite,
    primaryContainer = PastelPinkLight,
    onPrimaryContainer = PastelPinkDark,

    secondary = PastelLavenderDark,
    onSecondary = PastelWhite,
    secondaryContainer = PastelLavenderLight,
    onSecondaryContainer = DreamyTextPrimary,

    tertiary = PastelSkyBlue,
    onTertiary = PastelWhite,
    tertiaryContainer = PastelSkyBlueLight,
    onTertiaryContainer = DreamyTextPrimary,

    background = PastelPinkBg,
    onBackground = DreamyTextPrimary,

    surface = PastelWhite,
    onSurface = DreamyTextPrimary,
    surfaceVariant = PastelLavenderLight,
    onSurfaceVariant = DreamyTextSecondary
)

@Composable
fun DreamyTacToeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DreamyColorScheme,
        typography = AppTypography,
        content = content
    )
}
