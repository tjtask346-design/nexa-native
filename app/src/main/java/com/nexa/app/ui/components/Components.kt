package com.nexa.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
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
   ICON BUTTON (40x40 rounded)
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
   PIN DOTS — 5 circles, filled green
   ═══════════════════════════════════════ */
@Composable
fun PinDots(
    filled: Int,
    error: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (error) NexaRed else NexaGreen
    val emptyBorder = if (error) NexaRed else NexaBorder.copy(alpha = 0.22f)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(5) { i ->
            val isFilled = i < filled
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
                label = "dot$i"
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(if (isFilled) color else Color.Transparent)
                    .border(2.dp, if (isFilled) color else emptyBorder, CircleShape)
                    .then(
                        if (isFilled) Modifier.graphicsLayer {
                            shadowElevation = 12f
                            shape = CircleShape
                            clip = false
                            ambientShadowColor = color
                            spotShadowColor = color
                        } else Modifier
                    )
            )
        }
    }
}

/* ═══════════════════════════════════════
   PIN KEYPAD — 3x4 grid, one fingerprint key
   ═══════════════════════════════════════ */
@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onFingerprint: (() -> Unit)? = null,
    fingerprintActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEach { d ->
                    KeypadKey(label = d, onClick = { onDigit(d) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            if (onFingerprint != null) {
                FingerprintKey(
                    onClick = onFingerprint,
                    active = fingerprintActive,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(Modifier.weight(1f))
            }
            KeypadKey(label = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
            DeleteKey(onClick = onDelete, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeypadKey(label: String, onClick: () -> Unit, modifier: Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "key")

    Box(
        modifier = modifier
            .aspectRatio(1.45f)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(if (pressed) NexaSurface3 else NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = if (pressed) 0.22f else 0.09f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = NexaText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FingerprintKey(onClick: () -> Unit, active: Boolean, modifier: Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "fpk")

    // Pulse animation when active
    val transition = rememberInfiniteTransition(label = "fp")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "fpPulse"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.45f)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(NexaGreen.copy(alpha = if (active) 0.2f else 0.08f))
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) NexaGreen else NexaBorder.copy(alpha = 0.24f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Box(
                Modifier
                    .fillMaxSize()
                    .scale(1f + pulse * 0.25f)
                    .alpha(1f - pulse)
                    .border(2.dp, NexaGreen.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            )
        }
        Icon(
            Icons.Filled.Fingerprint,
            contentDescription = "Fingerprint",
            tint = NexaGreen,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun DeleteKey(onClick: () -> Unit, modifier: Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "del")

    Box(
        modifier = modifier
            .aspectRatio(1.45f)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(if (pressed) NexaSurface3 else NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Backspace,
            contentDescription = "Delete",
            tint = NexaMuted,
            modifier = Modifier.size(26.dp)
        )
    }
}

/* ═══════════════════════════════════════
   NEXA TEXT FIELD — with focus glow
   ═══════════════════════════════════════ */
@Composable
fun NexaTextField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    error: Boolean = false
) {
    val focused = remember { mutableStateOf(false) }
    val borderColor = when {
        error -> NexaRed
        focused.value -> NexaGreen.copy(alpha = 0.55f)
        else -> NexaBorder.copy(alpha = 0.09f)
    }
    val bgColor = if (focused.value) NexaSurface2 else NexaSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(17.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        if (leadingIcon != null) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                leadingIcon()
            }
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = NexaText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(NexaGreen),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused.value = it.isFocused }
                .padding(vertical = 17.dp)
        ) {
            if (value.isEmpty()) {
                Text(placeholder, color = NexaDim, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ═══════════════════════════════════════
   MARK ICON — payment method tile with logo fallback
   ═══════════════════════════════════════ */
@Composable
fun MethodMark(
    imageName: String?,   // e.g. "bkash" for R.drawable.bkash
    fallbackLetter: String,
    color: Color,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 14.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 17.sp,
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
            val id = androidx.compose.ui.platform.LocalContext.current.resources
                .getIdentifier(imageName, "drawable", androidx.compose.ui.platform.LocalContext.current.packageName)
            if (id != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id),
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
