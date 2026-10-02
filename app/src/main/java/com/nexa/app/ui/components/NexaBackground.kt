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
        // ═══ GLOW LAYER — bottom ═══
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {

                    // ─── GREEN GLOW (top-left) ───
                    val gC = Offset(0f, 0f)
                    val gR = size.width * 1.10f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to NexaGreen.copy(alpha = 0.50f),
                                0.25f to NexaGreen.copy(alpha = 0.28f),
                                0.50f to NexaGreen.copy(alpha = 0.12f),
                                0.75f to NexaGreen.copy(alpha = 0.04f),
                                1.00f to Color.Transparent
                            ),
                            center = gC,
                            radius = gR
                        ),
                        radius = gR,
                        center = gC
                    )

                    // ─── TEAL GLOW (top-right) ───
                    val tC = Offset(size.width, size.height * 0.22f)
                    val tR = size.width * 0.95f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to NexaTeal.copy(alpha = 0.42f),
                                0.25f to NexaTeal.copy(alpha = 0.24f),
                                0.50f to NexaTeal.copy(alpha = 0.10f),
                                0.75f to NexaTeal.copy(alpha = 0.035f),
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

        // ═══ CONTENT — top ═══
        content()
    }
}
