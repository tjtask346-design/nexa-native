package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexa.app.ui.theme.NexaBg
import com.nexa.app.ui.theme.NexaGreen
import com.nexa.app.ui.theme.NexaTeal

@Composable
fun NexaScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(NexaBg)) {
        // Top-left green glow
        Box(
            Modifier
                .align(Alignment.TopStart)
                .size(360.dp)
                .blur(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NexaGreen.copy(alpha = 0.22f), Color.Transparent),
                        radius = 500f
                    )
                )
        )
        // Right teal glow
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(320.dp)
                .blur(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NexaTeal.copy(alpha = 0.16f), Color.Transparent),
                        radius = 440f
                    )
                )
        )
        content()
    }
}
