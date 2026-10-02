package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.nexa.app.ui.theme.NexaBg

@Composable
fun NexaScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(NexaBg)
    ) {
        // 🧪 DIAGNOSTIC — bright red circle, unmissable
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = Color(0xFFFF0000),        // pure red
                        radius = size.width * 0.60f,       // 60% screen width
                        center = Offset(0f, 0f)            // top-left corner
                    )
                }
        )

        // Actual content on top
        content()
    }
}
