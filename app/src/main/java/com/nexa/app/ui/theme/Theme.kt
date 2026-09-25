package com.nexa.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NexaGreen = Color(0xFF4ADE80)
val NexaGreenDark = Color(0xFF22C55E)
val NexaTeal = Color(0xFF2DD4BF)
val NexaBg = Color(0xFF040A07)
val NexaSurface = Color(0xFF0B1C16)
val NexaSurface2 = Color(0xFF0F261D)
val NexaText = Color(0xFFEEFBF4)
val NexaMuted = Color(0xFF7FA694)
val NexaDim = Color(0xFF4D6B5E)
val NexaRed = Color(0xFFF87171)

private val NexaColors = darkColorScheme(
    primary = NexaGreen,
    onPrimary = Color(0xFF04140D),
    secondary = NexaTeal,
    background = NexaBg,
    surface = NexaSurface,
    onBackground = NexaText,
    onSurface = NexaText,
    error = NexaRed
)

@Composable
fun NexaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexaColors,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                letterSpacing = (-1.5).sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        ),
        content = content
    )
}
