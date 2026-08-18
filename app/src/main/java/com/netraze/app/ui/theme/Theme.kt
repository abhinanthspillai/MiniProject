package com.netraze.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnBlue,
    primaryContainer = FormSurfaceBlue,
    onPrimaryContainer = TextOnBlue,
    secondary = TextSecondary,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextOnBlue
)

@Composable
fun NetrazeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = NetrazeTypography,
        shapes = NetrazeShapes,
        content = content
    )
}
