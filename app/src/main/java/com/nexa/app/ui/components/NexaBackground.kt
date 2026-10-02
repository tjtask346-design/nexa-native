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
    ) {
        // ═══ GLOW LAYER (behind content) ═══
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {

                    // ─── GREEN GLOW — top-left corner ───
                    // Large radius (1.6x screen) + high alpha (0.55) → strong spread
                    val gC = Offset(-size.width * 0.15f, -size.height * 0.05f)
                    val gR = size.width * 1.60f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to NexaGreen.copy(alpha = 0.55f),
                                0.10f to NexaGreen.copy(alpha = 0.48f),
                                0.22f to NexaGreen.copy(alpha = 0.38f),
                                0.35f to NexaGreen.copy(alpha = 0.28f),
                                0.50f to NexaGreen.copy(alpha = 0.18f),
                                0.65f to NexaGreen.copy(alpha = 0.10f),
                                0.80f to NexaGreen.copy(alpha = 0.04f),
                                0.92f to NexaGreen.copy(alpha = 0.015f),
                                1.00f to Color.Transparent
                            ),
                            center = gC,
                            radius = gR
                        ),
                        radius = gR,
                        center = gC
                    )

                    // ─── TEAL GLOW — top-right corner ───
                    val tC = Offset(size.width * 1.15f, size.height * 0.15f)
                    val tR = size.width * 1.35f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to NexaTeal.copy(alpha = 0.48f),
                                0.10f to NexaTeal.copy(alpha = 0.42f),
                                0.22f to NexaTeal.copy(alpha = 0.32f),
                                0.35f to NexaTeal.copy(alpha = 0.23f),
                                0.50f to NexaTeal.copy(alpha = 0.15f),
                                0.65f to NexaTeal.copy(alpha = 0.08f),
                                0.80f to NexaTeal.copy(alpha = 0.03f),
                                0.92f to NexaTeal.copy(alpha = 0.01f),
                                1.00f to Color.Transparent
                            ),
                            center = tC,
                            radius = tR
                        ),
                        radius = tR,
                        center = tC
                    )
                }
        )

        // ═══ Content (drawn on top of glow) ═══
        content()
    }
}
