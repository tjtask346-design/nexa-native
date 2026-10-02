package com.nexa.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
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
            .drawWithContent {
                // ═══ STEP 1: Base background ═══
                drawRect(color = NexaBg)

                // ═══ STEP 2: GREEN GLOW (top-left) ═══
                val greenCenter = Offset(
                    x = size.width * 0.00f,
                    y = size.height * 0.02f
                )
                val greenRadius = size.width * 1.30f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.55f),
                            0.15f to NexaGreen.copy(alpha = 0.40f),
                            0.30f to NexaGreen.copy(alpha = 0.26f),
                            0.45f to NexaGreen.copy(alpha = 0.15f),
                            0.60f to NexaGreen.copy(alpha = 0.08f),
                            0.75f to NexaGreen.copy(alpha = 0.035f),
                            0.88f to NexaGreen.copy(alpha = 0.012f),
                            1.00f to Color.Transparent
                        ),
                        center = greenCenter,
                        radius = greenRadius
                    ),
                    radius = greenRadius,
                    center = greenCenter
                )

                // ═══ STEP 3: TEAL GLOW (top-right) ═══
                val tealCenter = Offset(
                    x = size.width * 1.00f,
                    y = size.height * 0.22f
                )
                val tealRadius = size.width * 1.05f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaTeal.copy(alpha = 0.48f),
                            0.15f to NexaTeal.copy(alpha = 0.34f),
                            0.30f to NexaTeal.copy(alpha = 0.22f),
                            0.45f to NexaTeal.copy(alpha = 0.13f),
                            0.60f to NexaTeal.copy(alpha = 0.065f),
                            0.75f to NexaTeal.copy(alpha = 0.03f),
                            0.88f to NexaTeal.copy(alpha = 0.01f),
                            1.00f to Color.Transparent
                        ),
                        center = tealCenter,
                        radius = tealRadius
                    ),
                    radius = tealRadius,
                    center = tealCenter
                )

                // ═══ STEP 4: Draw children ON TOP ═══
                drawContent()
            }
    ) {
        content()
    }
}
