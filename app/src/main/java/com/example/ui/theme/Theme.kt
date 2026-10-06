package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EcuLabColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextWhite,
    primaryContainer = CardElevatedSurface,
    onPrimaryContainer = CyanGlow,
    secondary = SignalGreen,
    onSecondary = DeepObsidian,
    secondaryContainer = SignalGreenBg,
    onSecondaryContainer = SignalGreen,
    tertiary = CyanGlow,
    background = DeepObsidian,
    onBackground = TextWhite,
    surface = CardDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = ButtonDarkSurface,
    onSurfaceVariant = TextSecondary,
    error = StopRed,
    onError = TextWhite,
    outline = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EcuLabColorScheme,
        typography = Typography,
        content = content
    )
}
