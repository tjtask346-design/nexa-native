package com.nexa.app.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * HTML:  @keyframes fadeUp
 * Usage: Modifier.fadeUp(delayMs = 400)
 */
@Composable
fun Modifier.fadeUp(delayMs: Int = 0, durationMs: Int = 700): Modifier {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        visible.value = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible.value) 1f else 0f,
        animationSpec = tween(durationMs, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "fadeUpAlpha"
    )
    val offset by animateFloatAsState(
        targetValue = if (visible.value) 0f else 14f,
        animationSpec = tween(durationMs, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "fadeUpOffset"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        translationY = offset
    }
}

/**
 * HTML:  @keyframes logoIn
 * scale 0.6→1, translateY 20→0, blur 12→0, alpha 0→1
 */
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

/**
 * HTML:  @keyframes shine — a skewed white gradient moving left→right forever
 * Usage: Box(Modifier.shineOverlay())
 */
fun Modifier.shineOverlay(
    durationMs: Int = 3200,
    delayMs: Int = 0
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shine")
    val x by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing, delayMillis = delayMs),
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
        // Skew is faked by drawing a rotated rect — simpler: just draw vertical band
        drawRect(
            brush = brush,
            topLeft = Offset(0f, 0f),
            size = size.copy(width = w, height = h)
        )
    }
}

/**
 * HTML:  @keyframes loadSlide — sliding bar inside track
 */
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
    Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(barColors),
                        topLeft = Offset(start, 0f),
                        size = androidx.compose.ui.geometry.Size(barW, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                    )
                }
        )
    }
}

/**
 * HTML:  @keyframes shake  (used on PIN error)
 * Returns a Modifier that shakes once when [trigger] changes to a non-zero value.
 */
fun Modifier.shake(trigger: Int): Modifier = composed {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            offset.snapTo(0f)
            offset.animateTo(0f, animationSpec = keyframes {
                durationMillis = 400
                -7f at 80
                7f at 240
                -3f at 320
                0f at 400
            })
        }
    }
    this.graphicsLayer { translationX = offset.value }
}

/**
 * HTML:  @keyframes fpPulse — fingerprint scanning pulse
 * Returns scale for pulse ring.
 */
@Composable
fun rememberFpPulse(active: Boolean): Float {
    val transition = rememberInfiniteTransition(label = "fpPulse")
    if (!active) return 0f
    val v by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fpPulseV"
    )
    return v
}

/**
 * HTML:  @keyframes scanMove — scanning line moving vertically
 */
@Composable
fun rememberScanProgress(): Float {
    val transition = rememberInfiniteTransition(label = "scan")
    val v by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanY"
    )
    return v
}

/**
 * HTML:  @keyframes draw — checkmark circle+path drawing
 * Returns progress 0..1
 */
@Composable
fun rememberDrawProgress(durationMs: Int = 650, delayMs: Int = 0): Float {
    val v = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        v.animateTo(1f, tween(durationMs, easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)))
    }
    return v.value
}
