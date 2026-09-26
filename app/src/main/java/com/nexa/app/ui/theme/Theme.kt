package com.nexa.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
        shapes = NexaShapes,
        content = content
    )
}
