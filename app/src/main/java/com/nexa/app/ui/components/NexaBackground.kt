package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nexa.app.ui.theme.NexaBg
import com.nexa.app.ui.theme.NexaGreen
import com.nexa.app.ui.theme.NexaTeal

@Composable
fun NexaScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(NexaBg)
            .drawBehind {
                // ═══ TOP-LEFT GREEN GLOW (soft, multi-stop) ═══
                val glRadius = size.width * 1.15f
                val glCenter = Offset(-size.width * 0.15f, -size.height * 0.10f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.26f),
                            0.25f to NexaGreen.copy(alpha = 0.16f),
                            0.50f to NexaGreen.copy(alpha = 0.07f),
                            0.75f to NexaGreen.copy(alpha = 0.02f),
                            1.00f to Color.Transparent
                        ),
                        center = glCenter,
                        radius = glRadius
                    ),
                    radius = glRadius,
                    center = glCenter
                )

                // ═══ TOP-RIGHT TEAL GLOW ═══
                val trRadius = size.width * 0.95f
                val trCenter = Offset(size.width * 1.20f, size.height * 0.18f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaTeal.copy(alpha = 0.20f),
                            0.25f to NexaTeal.copy(alpha = 0.12f),
                            0.50f to NexaTeal.copy(alpha = 0.05f),
                            0.75f to NexaTeal.copy(alpha = 0.015f),
                            1.00f to Color.Transparent
                        ),
                        center = trCenter,
                        radius = trRadius
                    ),
                    radius = trRadius,
                    center = trCenter
                )
            }
    ) {
        content()
    }
}
