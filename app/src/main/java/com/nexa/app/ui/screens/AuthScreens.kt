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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaTextField
import com.nexa.app.ui.theme.*

/* ─── Shared header for auth screens ─── */
@Composable
private fun AuthHeader(subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .fadeUp(0)
        ) {
            Image(
                painter = painterResource(R.drawable.nexa_logo),
                contentDescription = "Nexa",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "NEXA",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp,
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.horizontalGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal))
            ),
            modifier = Modifier.fadeUp(100)
        )
        Spacer(Modifier.height(34.dp))
        Text(
            text = subtitle,
            color = NexaText,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.7).sp,
            modifier = Modifier.fadeUp(150)
        )
    }
}

/* ═══════════════════════════════════════
   LOGIN
   ═══════════════════════════════════════ */
@Composable
fun LoginScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf(prefs.email ?: "") }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(horizontal = 26.dp)
    ) {
        AuthHeader("Welcome back 👋")

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Enter your email to continue.\nYou'll verify with your 5-digit PIN.",
            color = NexaMuted,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            modifier = Modifier.fadeUp(200)
        )

        Spacer(Modifier.height(30.dp))

        NexaTextField(
            value = email,
            onChange = { email = it },
            placeholder = "you@example.com",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Text("✉", color = NexaDim, fontSize = 15.sp)
            },
            modifier = Modifier.fadeUp(280)
        )

        Spacer(Modifier.height(20.dp))

        GradientButton(
            text = "Continue with Email",
            enabled = email.isNotBlank(),
            onClick = {
                val e = email.trim()
                AuthState.pendingEmail = e
                AuthState.pinMode = "login"
                prefs.email = e
                nav.navigate(Routes.PIN)
            },
            modifier = Modifier.fadeUp(340)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth().fadeUp(400),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                "Sign up",
                color = NexaGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickableNoRipple { nav.navigate(Routes.SIGNUP) }
            )
        }

        Spacer(Modifier.weight(1f))

        // Trust chips
        Row(
            Modifier.fillMaxWidth().padding(bottom = 40.dp).fadeUp(460),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            TrustChip("🔒", "256-bit")
            TrustChip("👆", "Biometric")
            TrustChip("✅", "KYC")
        }
    }
}

/* ═══════════════════════════════════════
   SIGNUP
   ═══════════════════════════════════════ */
@Composable
fun SignupScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(horizontal = 26.dp)
    ) {
        AuthHeader("Create your account")

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Email-only registration — done in 30 seconds.\nThen set your secure 5-digit PIN.",
            color = NexaMuted,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            modifier = Modifier.fadeUp(200)
        )

        Spacer(Modifier.height(28.dp))

        NexaTextField(
            value = email,
            onChange = { email = it },
            placeholder = "you@example.com",
            keyboardType = KeyboardType.Email,
            leadingIcon = { Text("✉", color = NexaDim, fontSize = 15.sp) },
            modifier = Modifier.fadeUp(280)
        )

        Spacer(Modifier.height(14.dp))

        NexaTextField(
            value = name,
            onChange = { name = it },
            placeholder = "Full name",
            leadingIcon = { Text("👤", color = NexaDim, fontSize = 15.sp) },
            modifier = Modifier.fadeUp(320)
        )

        Spacer(Modifier.height(20.dp))

        GradientButton(
            text = "Create Free Account",
            enabled = email.isNotBlank() && name.isNotBlank(),
            onClick = {
                val e = email.trim()
                val n = name.trim()
                AuthState.pendingEmail = e
                AuthState.pendingName = n
                AuthState.pinMode = "setup"
                prefs.email = e
                prefs.name = n
                nav.navigate(Routes.PIN)
            },
            modifier = Modifier.fadeUp(380)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth().fadeUp(440),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                "Log in",
                color = NexaGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickableNoRipple { nav.navigate(Routes.LOGIN) }
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            Modifier.fillMaxWidth().padding(bottom = 40.dp).fadeUp(500),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            TrustChip("⚡", "0% fee")
            TrustChip("🌐", "LTC · USDT")
            TrustChip("📱", "bKash")
        }
    }
}

/* ─── Small helpers ─── */
@Composable
private fun TrustChip(emoji: String, text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(NexaSurface)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 11.sp)
        Text(text, color = NexaMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/* clickable with no ripple */
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.then(
        androidx.compose.foundation.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick
        )
    )
}
