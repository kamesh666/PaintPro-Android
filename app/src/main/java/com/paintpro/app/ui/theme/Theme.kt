package com.paintpro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PaintProBlue,
    onPrimary = PaintProOnPrimary,
    secondary = PaintProBlueDark,
    background = PaintProBackground,
    surface = PaintProSurface,
    error = PaintProError,
    outline = PaintProOutline,
    onSurfaceVariant = PaintProOnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = PaintProBlue,
    onPrimary = PaintProOnPrimary,
    secondary = PaintProBlueDark,
)

@Composable
fun PaintProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaintProTypography,
        content = content,
    )
}
