package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = WhitePure,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = NavyPrimary,
    secondary = BlueAccent,
    onSecondary = WhitePure,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = SkyAccent,
    tertiary = VioletAccent,
    onTertiary = WhitePure,
    background = WhitePure,
    onBackground = Slate900,
    surface = WhitePure,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate100,
    error = RoseError,
    onError = WhitePure
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = DarkBackground,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = SkyAccentDark,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFFA5B4FC),
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = Slate50,
    surface = DarkSurface,
    onSurface = Slate50,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate300,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    onError = DarkBackground
)

@Composable
fun AILifeAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
