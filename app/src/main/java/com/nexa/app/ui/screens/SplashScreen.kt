package com.nexa.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.R
import com.nexa.app.ui.animations.SlideLoaderTrack
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.animations.logoIn
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
                // ═══ Center background radial — subtle green ambient ═══
                // center (50%, 45%), green@12 → transparent, radius ~75% width
                val radius = size.width * 0.75f
                val c = Offset(size.width * 0.5f, size.height * 0.45f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.12f),
                            0.55f to NexaGreen.copy(alpha = 0.04f),
                            1.00f to Color.Transparent
                        ),
                        center = c,
                        radius = radius
                    ),
                    radius = radius,
                    center = c
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // ═══════════════════════════════════════════════
            // LOGO with MOON HALO — spreading like moonlight
            // ═══════════════════════════════════════════════
            Box(
                Modifier.size(270.dp),  // extra canvas for halo spread
                contentAlignment = Alignment.Center
            ) {
                // ═══ Moon halo layer (background) ═══
                // 9-stop radial gradient — soft fade across large area
                Box(
                    Modifier
                        .fillMaxSize()
                        .logoIn(1000)
                        .drawBehind {
                            val radius = this.size.minDimension / 2f
                            val c = this.center
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
                                    center = c,
                                    radius = radius
                                ),
                                radius = radius,
                                center = c
                            )
                        }
                )

                // ═══ Actual logo (on top of halo) ═══
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

            // ═══ NEXA wordmark — gradient text ═══
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

        // ═══ Bottom loader — animated slide bar ═══
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
