package com.nexa.app.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@Composable
fun Modifier.fadeUp(delayMs: Int = 0, durationMs: Int = 700): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMs, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "fadeUpAlpha"
    )
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else 14f,
        animationSpec = tween(durationMs, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "fadeUpOffset"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        translationY = offset
    }
}

@Composable
fun Modifier.logoIn(durationMs: Int = 1000): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            animationSpec = tween(durationMs, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
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

fun Modifier.shineOverlay(durationMs: Int = 3200): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shine")
    val x by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineX"
    )
    this.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val bandW = w * 0.4f
        val start = w * x
        val brush = Brush.horizontalGradient(
            0f to Color.Transparent,
            0.5f to Color.White.copy(alpha = 0.45f),
            1f to Color.Transparent,
            startX = start,
            endX = start + bandW
        )
        drawRect(brush = brush, topLeft = Offset(0f, 0f), size = Size(w, h))
    }
}

@Composable
fun SlideLoaderTrack(
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFF153025),
    barColors: List<Color> = listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF2DD4BF))
) {
    val transition = rememberInfiniteTransition(label = "loadSlide")
    val pos by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadX"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val barW = w * 0.5f
                    val start = w * pos
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = CornerRadius(h / 2)
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(barColors),
                        topLeft = Offset(start, 0f),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(h / 2)
                    )
                }
        )
    }
}

fun Modifier.shake(trigger: Int): Modifier = composed {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            offset.snapTo(0f)
            offset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -7f at 80
                    7f at 240
                    -3f at 320
                    0f at 400
                }
            )
        }
    }
    this.graphicsLayer { translationX = offset.value }
}
