package com.stockflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = Paper,
    primaryContainer = Teal50,
    onPrimaryContainer = TealDark,
    secondary = Clay500,
    onSecondary = Paper,
    secondaryContainer = Clay50,
    onSecondaryContainer = ClayDark,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperVariant,
    onSurfaceVariant = InkMuted,
    surfaceContainerLow = Color.White,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = Teal200,
    onPrimary = TealDark,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal50,
    secondary = Clay200,
    onSecondary = ClayDark,
    secondaryContainer = ClayDark,
    onSecondaryContainer = Clay50,
    background = Night,
    onBackground = NightText,
    surface = Night,
    onSurface = NightText,
    surfaceVariant = NightVariant,
    onSurfaceVariant = NightMuted,
    surfaceContainerLow = NightSurface,
    error = ErrorRedLight
)

@Composable
fun StockFlowTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = StockFlowTypography,
        content = content
    )
}
