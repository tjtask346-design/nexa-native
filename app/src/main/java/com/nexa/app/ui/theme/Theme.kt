package com.nexa.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NexaColors = darkColorScheme(
    primary          = NexaGreen,
    onPrimary        = Color(0xFF04140D),
    secondary        = NexaTeal,
    background       = NexaBg,
    surface          = NexaSurface,
    surfaceVariant   = NexaSurface2,
    onBackground     = NexaText,
    onSurface        = NexaText,
    onSurfaceVariant = NexaMuted,
    error            = NexaRed,
    outline          = NexaDim
)

// Plus Jakarta Sans substitute: system sans with proper weights
val NexaFont = FontFamily.SansSerif

val NexaShapes = Shapes(
    extraSmall = RoundedCornerShape(11.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(17.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun NexaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexaColors,
        typography = MaterialTheme.typography.copy(
            displayLarge  = TextStyle(NexaFont, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, letterSpacing = (-1.6).sp),
            headlineLarge = TextStyle(NexaFont, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = 8.sp),
            headlineMedium= TextStyle(NexaFont, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = (-0.7).sp),
            titleLarge    = TextStyle(NexaFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
            titleMedium   = TextStyle(NexaFont, fontWeight = FontWeight.Bold,     fontSize = 17.sp, letterSpacing = (-0.3).sp),
            titleSmall    = TextStyle(NexaFont, fontWeight = FontWeight.Bold,     fontSize = 15.sp, letterSpacing = (-0.2).sp),
            bodyLarge     = TextStyle(NexaFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
            bodyMedium    = TextStyle(NexaFont, fontWeight = FontWeight.Medium,   fontSize = 13.sp),
            bodySmall     = TextStyle(NexaFont, fontWeight = FontWeight.Medium,   fontSize = 11.5.sp),
            labelSmall    = TextStyle(NexaFont, fontWeight = FontWeight.Bold,     fontSize = 10.sp, letterSpacing = 0.2.sp)
        ),
        shapes = NexaShapes,
        content = content
    )
}
