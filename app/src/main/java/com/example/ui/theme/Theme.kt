package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val TradePulseColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = GainGreen,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyan,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle.copy(alpha = 0.5f),
    error = LossRed,
    onError = Color.White
)

@Composable
fun TradePulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TradePulseColorScheme,
        typography = Typography,
        content = content
    )
}

