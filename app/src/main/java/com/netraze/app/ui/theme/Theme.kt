package com.netraze.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = TextOnDark,
    primaryContainer = SurfaceWhite,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceTranslucent,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextOnDark
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
