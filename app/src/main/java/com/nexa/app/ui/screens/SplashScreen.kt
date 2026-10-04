package com.nexa.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.R
import com.nexa.app.ui.animations.SlideLoaderTrack
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2400)
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .drawBehind {
                // ═══ Background center radial (green@12, center 50%/45%, radius 60%) ═══
                val bgRadius = size.width * 0.6f
                val bgCenter = Offset(size.width * 0.5f, size.height * 0.45f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.12f),
                            0.60f to NexaGreen.copy(alpha = 0.04f),
                            1.00f to Color.Transparent
                        ),
                        center = bgCenter,
                        radius = bgRadius
                    ),
                    radius = bgRadius,
                    center = bgCenter
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // ═══════════════════════════════════════════
            // LOGO with MOON HALO — soft, wide, radiated glow
            // ═══════════════════════════════════════════
            Box(
                Modifier.size(270.dp),  // extra space for halo to spread
                contentAlignment = Alignment.Center
            ) {
                // ─── Moon halo (spreads all around, soft falloff) ───
                Box(
                    Modifier
                        .fillMaxSize()
                        .logoIn(1000)
                        .drawBehind {
                            val radius = size.minDimension / 2f
                            val center = center
                            // Multi-stop radial — smooth fade like moonlight
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.00f to NexaGreen.copy(alpha = 0.55f),
                                        0.10f to NexaGreen.copy(alpha = 0.48f),
                                        0.22f to NexaGreen.copy(alpha = 0.35f),
                                        0.35f to NexaGreen.copy(alpha = 0.22f),
                                        0.48f to NexaGreen.copy(alpha = 0.13f),
                                        0.62f to NexaGreen.copy(alpha = 0.07f),
                                        0.75f to NexaGreen.copy(alpha = 0.035f),
                                        0.88f to NexaGreen.copy(alpha = 0.012f),
                                        1.00f to Color.Transparent
                                    ),
                                    center = center,
                                    radius = radius
                                ),
                                radius = radius,
                                center = center
                            )
                        }
                )

                // ─── Actual logo (centered) ───
                Box(
                    Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .logoIn(1000)
                ) {
                    Image(
                        painter = painterResource(R.drawable.nexa_logo),
                        contentDescription = "Nexa",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            // ═══ NEXA wordmark with gradient ═══
            Text(
                text = "NEXA",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        listOf(NexaGreen, NexaGreenDark, NexaTeal)
                    )
                ),
                modifier = Modifier.fadeUp(delayMs = 400)
            )

            Spacer(Modifier.height(6.dp))

            // ═══ Tagline ═══
            Text(
                text = "MOVE MONEY FREELY",
                color = NexaMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.5.sp,
                modifier = Modifier.fadeUp(delayMs = 550)
            )
        }

        // ═══ Bottom loader with green glow ═══
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .fadeUp(delayMs = 700)
        ) {
            SlideLoaderTrack(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

// ═══════════════════════════════════════════════
// LOGO-IN ANIMATION — scale, fade, translateY (with blur start)
// ═══════════════════════════════════════════════
@Composable
private fun Modifier.logoIn(durationMs: Int = 1000): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
            )
        )
    }
    val p = progress.value
    return this.graphicsLayer {
        alpha = p
        val scale = 0.6f + 0.4f * p
        scaleX = scale
        scaleY = scale
        translationY = (1f - p) * 20f
    }
}
