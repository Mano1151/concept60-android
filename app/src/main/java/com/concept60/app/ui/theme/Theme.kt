package com.concept60.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Brand colors matching the React app
val Accent = Color(0xFF6C63FF)
val AccentLight = Color(0xFF8B85FF)
val AccentCyan = Color(0xFF00E6FF)
val BgDark = Color(0xFF0F0F0F)
val PanelDark = Color(0xFF1A1A1A)
val SurfaceDark = Color(0xFF121212)
val OnSurfaceDark = Color(0xFFF5F5F5)
val SubtextDark = Color(0xFF94A3B8)
val BorderDark = Color(0x1AFFFFFF)
val ErrorColor = Color(0xFFFB7185)
val SuccessColor = Color(0xFF34D399)

val BgLight = Color(0xFFEDF2FF)
val PanelLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF111827)
val SubtextLight = Color(0xFF6B7280)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = AccentCyan,
    onSecondary = BgDark,
    background = BgDark,
    onBackground = OnSurfaceDark,
    surface = PanelDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF1E1E2E),
    onSurfaceVariant = SubtextDark,
    error = ErrorColor,
    outline = BorderDark,
)

private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = OnSurfaceLight,
    surface = PanelLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF0F0FF),
    onSurfaceVariant = SubtextLight,
    error = ErrorColor,
    outline = Color(0xFFE2E8F0),
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun Concept60Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
