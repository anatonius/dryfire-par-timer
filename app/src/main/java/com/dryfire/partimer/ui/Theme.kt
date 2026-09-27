package com.dryfire.partimer.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Design language matching the reference shooting-training app. */
object DryFireColors {
    val Background = Color(0xFF141414)
    val Surface = Color(0xFF2A2A2A)
    val SurfaceVariant = Color(0xFF3A3A3A)
    val TopBar = Color(0xFF3D3D3D)
    val Amber = Color(0xFFFFC107)
    val OnAmber = Color(0xFF000000)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFBDBDBD)
    val Outline = Color(0xFF5A5A5A)
    val StopRed = Color(0xFFD32F2F)
}

private val ColorScheme = darkColorScheme(
    primary = DryFireColors.Amber,
    onPrimary = DryFireColors.OnAmber,
    secondary = DryFireColors.Amber,
    onSecondary = DryFireColors.OnAmber,
    tertiary = DryFireColors.Amber,
    background = DryFireColors.Background,
    onBackground = DryFireColors.TextPrimary,
    surface = DryFireColors.Surface,
    onSurface = DryFireColors.TextPrimary,
    surfaceVariant = DryFireColors.SurfaceVariant,
    onSurfaceVariant = DryFireColors.TextSecondary,
    surfaceContainer = DryFireColors.Surface,
    surfaceContainerHigh = DryFireColors.SurfaceVariant,
    outline = DryFireColors.Outline,
    outlineVariant = DryFireColors.Outline
)

private val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun DryFireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        shapes = Shapes,
        content = content
    )
}
