package com.sandalia.stockanchor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand: deep harbour navy + sea teal, amber for warnings.
val Navy = Color(0xFF0F2A44)
val NavyLight = Color(0xFF1D4468)
val Teal = Color(0xFF0E9F8E)
val TealLight = Color(0xFF2FD3BF)
val Amber = Color(0xFFF2A93B)
val Coral = Color(0xFFE5484D)
val Positive = Color(0xFF12A150)

private val Light = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E5F3),
    onPrimaryContainer = Navy,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF3EE),
    onSecondaryContainer = Color(0xFF00382F),
    tertiary = Amber,
    tertiaryContainer = Color(0xFFFFEBCB),
    onTertiaryContainer = Color(0xFF4A2E00),
    error = Coral,
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF14202E),
    surface = Color.White,
    onSurface = Color(0xFF14202E),
    surfaceVariant = Color(0xFFEDF1F6),
    onSurfaceVariant = Color(0xFF5B6B7D),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F4F8),
    surfaceContainerHigh = Color(0xFFEBEFF4),
    outline = Color(0xFFC5CED9),
    outlineVariant = Color(0xFFE2E8EF),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9CC3EA),
    onPrimary = Color(0xFF00213D),
    primaryContainer = NavyLight,
    onPrimaryContainer = Color(0xFFD7E5F3),
    secondary = TealLight,
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF00544A),
    onSecondaryContainer = Color(0xFFCFF3EE),
    tertiary = Amber,
    tertiaryContainer = Color(0xFF5C3D00),
    onTertiaryContainer = Color(0xFFFFEBCB),
    error = Color(0xFFFF8A8E),
    background = Color(0xFF0B1622),
    onBackground = Color(0xFFE3E9F0),
    surface = Color(0xFF111F2E),
    onSurface = Color(0xFFE3E9F0),
    surfaceVariant = Color(0xFF1B2B3C),
    onSurfaceVariant = Color(0xFF9FB0C2),
    surfaceContainerLowest = Color(0xFF0D1926),
    surfaceContainerLow = Color(0xFF13212F),
    surfaceContainer = Color(0xFF172636),
    surfaceContainerHigh = Color(0xFF1D2D3E),
    outline = Color(0xFF3A4D61),
    outlineVariant = Color(0xFF263749),
)

private val base = Typography()
private val AppTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.6.sp),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun StockAnchorTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
