package com.nexa.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.ui.theme.*

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onFingerprint: (() -> Unit)? = null,
    fingerprintEnabled: Boolean = true,
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
            FingerprintKey(
                onClick = { if (fingerprintEnabled && onFingerprint != null) onFingerprint() },
                enabled = fingerprintEnabled && onFingerprint != null,
                active = fingerprintActive,
                modifier = Modifier.weight(1f)
            )
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
private fun FingerprintKey(
    onClick: () -> Unit,
    enabled: Boolean,
    active: Boolean,
    modifier: Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.94f else 1f, label = "fpk")

    val transition = rememberInfiniteTransition(label = "fp")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "fpPulse"
    )

    val alpha = if (enabled) 1f else 0.35f
    val bgAlpha = when {
        !enabled -> 0.03f
        active -> 0.20f
        else -> 0.08f
    }

    Box(
        modifier = modifier
            .aspectRatio(1.45f)
            .alpha(alpha)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(NexaGreen.copy(alpha = bgAlpha))
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) NexaGreen else NexaBorder.copy(alpha = 0.24f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
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
