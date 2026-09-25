package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.ui.theme.*

fun Modifier.clickableBack(onClick: () -> Unit): Modifier = this.clickable { onClick() }

@Composable
fun GradientButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brush = Brush.horizontalGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) brush else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFF04140D), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun PinDots(filled: Int, error: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(5) { i ->
            val isFilled = i < filled
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) (if (error) NexaRed else NexaGreen) else Color.Transparent)
                    .border(2.dp, if (error) NexaRed else if (isFilled) NexaGreen else NexaDim, CircleShape)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onFingerprint: (() -> Unit)? = null,
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
                row.forEach { d -> KeypadKey(label = d, onClick = { onDigit(d) }, modifier = Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            if (onFingerprint != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.45f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(NexaGreen.copy(alpha = 0.08f))
                        .border(1.dp, NexaGreen.copy(alpha = 0.24f), RoundedCornerShape(22.dp))
                        .clickable { onFingerprint() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = "Fingerprint", tint = NexaGreen, modifier = Modifier.size(30.dp))
                }
            } else {
                Box(Modifier.weight(1f))
            }
            KeypadKey(label = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.45f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaGreen.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Backspace, contentDescription = "Delete", tint = NexaMuted, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun KeypadKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1.45f)
            .clip(RoundedCornerShape(22.dp))
            .background(NexaSurface)
            .border(1.dp, NexaGreen.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = NexaText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MethodIcon(mark: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(mark, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
    }
}
