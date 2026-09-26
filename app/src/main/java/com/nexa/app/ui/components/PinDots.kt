package com.nexa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nexa.app.ui.theme.*

@Composable
fun PinDots(filled: Int, error: Boolean = false, modifier: Modifier = Modifier) {
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
