package com.nexa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexa.app.ui.theme.*

@Composable
fun NexaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    val edgePadding = 3.dp

    val fraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "switchFraction"
    )

    val trackColor = when {
        !enabled -> NexaSurface3.copy(alpha = 0.4f)
        checked -> NexaGreen
        else -> NexaSurface3
    }

    val borderColor = when {
        !enabled -> NexaBorder.copy(alpha = 0.1f)
        checked -> NexaGreen
        else -> NexaBorder.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(trackHeight / 2))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val offsetX = (trackWidth - thumbSize - edgePadding * 2) * fraction
        Box(
            Modifier
                .padding(start = edgePadding)
                .offset(x = offsetX)
                .size(thumbSize)
                .shadow(2.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
