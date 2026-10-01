package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
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
    Box(
        modifier
            .fillMaxSize()
            .background(NexaBg)
    ) {
        // ═══════════════════════════════════════════════
        // TOP-LEFT GREEN GLOW  ← HTML .phone::before
        // top:-190px  left:-90px  size:360x360  blur:34px
        // ═══════════════════════════════════════════════
        Box(
            Modifier
                .offset(x = (-90).dp, y = (-190).dp)
                .size(360.dp)
                .blur(
                    radius = 34.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded  // ← KEY FIX
                )
                .drawWithCache {
                    val brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.22f),
                            0.68f to Color.Transparent          // HTML: transparent 68%
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension / 2f
                    )
                    onDrawBehind {
                        drawCircle(
                            brush = brush,
                            radius = size.minDimension / 2f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
        )

        // ═══════════════════════════════════════════════
        // TOP-RIGHT TEAL GLOW  ← HTML .phone::after
        // top:130px  right:-150px  size:320x320  blur:34px
        // ═══════════════════════════════════════════════
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 150.dp, y = 130.dp)
                .size(320.dp)
                .blur(
                    radius = 34.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded  // ← KEY FIX
                )
                .drawWithCache {
                    val brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaTeal.copy(alpha = 0.16f),
                            0.70f to Color.Transparent          // HTML: transparent 70%
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension / 2f
                    )
                    onDrawBehind {
                        drawCircle(
                            brush = brush,
                            radius = size.minDimension / 2f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
        )

        // ═══ Actual screen content on top ═══
        content()
    }
}
