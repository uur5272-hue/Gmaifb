package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpaceBackground,
    primaryContainer = LaserBlue,
    onPrimaryContainer = Color.White,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = MetallicSurfaceHighlight,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonMint,
    onTertiary = DeepSpaceBackground,
    background = DeepSpaceBackground,
    onBackground = TextPrimary,
    surface = DarkMetallicSurface,
    onSurface = TextPrimary,
    surfaceVariant = MetallicSurfaceHighlight,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun MotoAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberpunkDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MotoAiTheme(content = content)
}
