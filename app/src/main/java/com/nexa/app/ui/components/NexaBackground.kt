package com.nexa.app.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
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
        // ══════════════════════════════════════════════════
        // GREEN GLOW — top-left (HTML: top:-190px left:-90px 360x360 blur34)
        // ══════════════════════════════════════════════════
        Box(
            Modifier
                .offset(x = (-110).dp, y = (-190).dp)
                .size(360.dp)
                .graphicsLayer {
                    // Real GPU blur — works on Android 12+
                    renderEffect = RenderEffect
                        .createBlurEffect(
                            90f, 90f,                  // sigma X, Y — very soft
                            Shader.TileMode.DECAL
                        )
                        .asComposeRenderEffect()
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NexaGreen.copy(alpha = 0.55f),
                            NexaGreen.copy(alpha = 0.35f),
                            NexaGreen.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ══════════════════════════════════════════════════
        // TEAL GLOW — top-right (HTML: top:130px right:-150px 320x320 blur34)
        // ══════════════════════════════════════════════════
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 150.dp, y = 130.dp)
                .size(320.dp)
                .graphicsLayer {
                    renderEffect = RenderEffect
                        .createBlurEffect(
                            90f, 90f,
                            Shader.TileMode.DECAL
                        )
                        .asComposeRenderEffect()
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NexaTeal.copy(alpha = 0.48f),
                            NexaTeal.copy(alpha = 0.30f),
                            NexaTeal.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ═══ Content on top ═══
        content()
    }
}
