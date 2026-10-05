package com.nexa.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.nav.Routes
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(nav: NavController, kind: String, amount: String, id: String) {
    val circleProgress = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        circleProgress.animateTo(1f, tween(650, easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)))
        delay(180)
        checkProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)))
    }

    val isDeposit = kind == "deposit"
    val title = if (isDeposit) "Deposit Submitted!" else "Withdrawal Requested!"
    val sub = if (isDeposit)
        "Admin will verify and credit your balance shortly"
    else
        "Usually processed within 5–30 minutes"

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated checkmark
        Canvas(modifier = Modifier.size(92.dp)) {
            val stroke = 4.dp.toPx()
            // circle
            drawArc(
                color = NexaGreen,
                startAngle = -90f,
                sweepAngle = 360f * circleProgress.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // check path
            if (checkProgress.value > 0f) {
                val w = size.width
                val h = size.height
                val p1 = Offset(w * 0.32f, h * 0.51f)
                val p2 = Offset(w * 0.445f, h * 0.635f)
                val p3 = Offset(w * 0.69f, h * 0.38f)
                val prog = checkProgress.value
                val firstHalf = (prog / 0.5f).coerceAtMost(1f)
                val secondHalf = ((prog - 0.5f) / 0.5f).coerceAtLeast(0f)

                drawLine(
                    color = NexaGreen,
                    start = p1,
                    end = Offset(
                        p1.x + (p2.x - p1.x) * firstHalf,
                        p1.y + (p2.y - p1.y) * firstHalf
                    ),
                    strokeWidth = stroke * 1.25f,
                    cap = StrokeCap.Round
                )
                if (secondHalf > 0f) {
                    drawLine(
                        color = NexaGreen,
                        start = p2,
                        end = Offset(
                            p2.x + (p3.x - p2.x) * secondHalf,
                            p2.y + (p3.y - p2.y) * secondHalf
                        ),
                        strokeWidth = stroke * 1.25f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            title,
            color = NexaText,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            letterSpacing = (-0.6).sp,
            modifier = Modifier.fadeUp(350)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            sub,
            color = NexaMuted,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fadeUp(450)
        )

        Spacer(Modifier.height(26.dp))

        // Summary card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(NexaSurface)
                .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .fadeUp(550)
        ) {
            Column {
                KV("Amount", "$${String.format("%,.2f", amount.toDoubleOrNull() ?: 0.0)}")
                KV("Method", "Nexa")
                KV("Transaction ID", id)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", color = NexaMuted, fontSize = 13.sp)
                    Text("Pending", color = NexaTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        GradientButton(
            text = "Back to Home",
            onClick = { nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
            modifier = Modifier.fadeUp(650)
        )
    }
}

@Composable
private fun KV(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = NexaText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
