package com.nexa.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.R
import com.nexa.app.ui.animations.SlideLoaderTrack
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.animations.logoIn
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2400)
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(NexaBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with logoIn animation
            Box(
                Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .logoIn(1000)
            ) {
                Image(
                    painter = painterResource(R.drawable.nexa_logo),
                    contentDescription = "Nexa",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(26.dp))

            // NEXA wordmark with gradient text
            Text(
                text = "NEXA",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal))
                ),
                modifier = Modifier.fadeUp(delayMs = 400)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "MOVE MONEY FREELY",
                color = NexaMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.5.sp,
                modifier = Modifier.fadeUp(delayMs = 550)
            )
        }

        // Centered loader at bottom
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .fadeUp(delayMs = 700)
        ) {
            SlideLoaderTrack(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
