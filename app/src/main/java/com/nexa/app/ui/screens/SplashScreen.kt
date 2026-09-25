package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onDone()
    }
    Box(
        Modifier.fillMaxSize().background(NexaBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
            )
            Spacer(Modifier.height(28.dp))
            Text("NEXA", color = NexaGreen, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = 8.sp)
            Spacer(Modifier.height(6.dp))
            Text("MOVE MONEY FREELY", color = NexaMuted, fontSize = 11.sp, letterSpacing = 3.sp)
        }
    }
}
