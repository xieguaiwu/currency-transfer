package com.xieguiawu.currencytransfer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xieguiawu.currencytransfer.R

/**
 * Retro pixel theme: PICO-8 inspired palette, Press Start 2P pixel font,
 * and square (zero-radius) shapes for an 8-bit console feel.
 */

// PICO-8 palette
private val PicoNavy = Color(0xFF1D2B53)      // background / surface
private val PicoDark = Color(0xFF0F1329)      // darker surface
private val PicoBlue = Color(0xFF29366F)      // elevated surface
private val PicoWhite = Color(0xFFF4F4F4)     // on-background
private val PicoGrey = Color(0xFFC2C3C7)      // secondary text
private val PicoYellow = Color(0xFFFFEC27)    // primary accent
private val PicoGreen = Color(0xFF00E436)     // tertiary accent
private val PicoCyan = Color(0xFF29ADFF)      // secondary accent
private val PicoRed = Color(0xFFFF004D)       // error
private val PicoPurple = Color(0xFF83769C)    // outline

private val RetroColors = darkColorScheme(
    primary = PicoYellow,
    onPrimary = PicoDark,
    primaryContainer = PicoBlue,
    onPrimaryContainer = PicoWhite,
    secondary = PicoCyan,
    onSecondary = PicoDark,
    secondaryContainer = PicoBlue,
    onSecondaryContainer = PicoWhite,
    tertiary = PicoGreen,
    onTertiary = PicoDark,
    background = PicoNavy,
    onBackground = PicoWhite,
    surface = PicoNavy,
    onSurface = PicoWhite,
    surfaceVariant = PicoBlue,
    onSurfaceVariant = PicoGrey,
    error = PicoRed,
    onError = PicoDark,
    outline = PicoPurple,
    outlineVariant = PicoPurple.copy(alpha = 0.5f),
)

/** Press Start 2P — 8x8 pixel font, OFL licensed, Latin/hiragana only. */
val PressStart2P = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal),
)

// Pixel font renders wide; use smaller sizes than the default typography.
// Body text stays on the system font for readability (ASD-STE100).
private val RetroTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = PressStart2P, fontSize = 14.sp, lineHeight = 20.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PressStart2P, fontSize = 12.sp, lineHeight = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PressStart2P, fontSize = 10.sp, lineHeight = 16.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PressStart2P, fontSize = 9.sp, lineHeight = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PressStart2P, fontSize = 9.sp, lineHeight = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PressStart2P, fontSize = 8.sp, lineHeight = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PressStart2P, fontSize = 7.sp, lineHeight = 11.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = PressStart2P, fontSize = 9.sp, lineHeight = 14.sp,
    ),
    // bodyMedium / bodySmall keep the default readable font
)

private val RetroShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun CurrencyTransferTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColors,
        typography = RetroTypography,
        shapes = RetroShapes,
        content = content,
    )
}
