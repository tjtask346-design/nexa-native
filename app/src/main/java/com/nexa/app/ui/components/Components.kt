package com.nexa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.ui.animations.shineOverlay
import com.nexa.app.ui.theme.*

/* ═══════════════════════════════════════
   GRADIENT BUTTON — with shine + press scale
   ═══════════════════════════════════════ */
@Composable
fun GradientButton(
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.975f else 1f, label = "btnScale")

    val grad = Brush.horizontalGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal))
    val disabledGrad = Brush.horizontalGradient(listOf(NexaDim, NexaDim))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(17.dp))
            .background(if (enabled && !loading) grad else disabledGrad)
            .then(if (enabled && !loading) Modifier.shineOverlay() else Modifier)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (loading) "Please wait…" else text,
            color = Color(0xFF04140D),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
    }
}

/* ═══════════════════════════════════════
   ICON BUTTON — 40x40 rounded, press scale
   ═══════════════════════════════════════ */
@Composable
fun NexaIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "ib")

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(NexaSurface2)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ═══════════════════════════════════════
   METHOD MARK — payment icon tile with logo fallback
   ═══════════════════════════════════════ */
@Composable
fun MethodMark(
    imageName: String?,
    fallbackLetter: String,
    color: Color,
    size: Dp = 42.dp,
    cornerRadius: Dp = 14.dp,
    fontSize: TextUnit = 17.sp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageName != null) {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val id = ctx.resources.getIdentifier(imageName, "drawable", ctx.packageName)
            if (id != 0) {
                Image(
                    painter = painterResource(id),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.78f)
                )
            } else {
                Text(fallbackLetter, color = color, fontWeight = FontWeight.ExtraBold, fontSize = fontSize)
            }
        } else {
            Text(fallbackLetter, color = color, fontWeight = FontWeight.ExtraBold, fontSize = fontSize)
        }
    }
}
