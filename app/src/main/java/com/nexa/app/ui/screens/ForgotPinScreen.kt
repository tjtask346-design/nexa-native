package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*

@Composable
fun ForgotPinScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf(AuthState.pendingEmail ?: prefs.email ?: "") }

    Column(
        Modifier.fillMaxSize().background(NexaBg).padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        NexaIconButton(onClick = { nav.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexaText, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(30.dp))

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(88.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha = 0.15f), NexaTeal.copy(alpha = 0.12f))))
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(42.dp))
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Reset your PIN",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
                letterSpacing = (-0.6).sp, modifier = Modifier.fadeUp(100)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Verify your identity with the 6-digit\ncode from your authenticator app.",
                color = NexaMuted, fontSize = 13.5.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.fadeUp(150)
            )
        }

        Spacer(Modifier.height(36.dp))

        // Email input
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                .background(NexaSurface)
                .border(1.5.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(17.dp))
                .padding(horizontal = 16.dp)
                .fadeUp(200)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Icon(Icons.Filled.Security, null, tint = NexaDim, modifier = Modifier.size(16.dp))
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    textStyle = TextStyle(color = NexaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    cursorBrush = SolidColor(NexaGreen),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f).padding(vertical = 17.dp)
                ) { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        if (email.isEmpty()) {
                            Text("you@example.com", color = NexaDim, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        inner()
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        GradientButton(
            text = "Continue with Authenticator",
            enabled = email.isNotBlank() && email.contains("@"),
            onClick = {
                AuthState.pendingEmail = email.trim()
                nav.navigate(Routes.RESET_PIN)
            },
            modifier = Modifier.fadeUp(260)
        )

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(NexaTeal.copy(alpha = 0.07f))
                .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                .padding(14.dp)
                .fadeUp(320)
        ) {
            Text(
                "You need access to your authenticator app to reset your PIN. If you've lost access, contact support.",
                color = androidx.compose.ui.graphics.Color(0xFF7DD3C8),
                fontSize = 11.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}
