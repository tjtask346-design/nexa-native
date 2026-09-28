package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    val email = prefs.email ?: ""

    fun check() {
        scope.launch {
            checking = true
            val ok = repo.isEmailVerified()
            if (!ok) { checking = false; Toast.makeText(ctx, "Email not verified yet.", Toast.LENGTH_SHORT).show(); return@launch }
            val name = AuthState.pendingName ?: prefs.name ?: email.substringBefore("@")
            val pin = prefs.pin ?: ""
            repo.register(email, name, pin).onSuccess { r ->
                checking = false
                if (r.success && r.token != null) {
                    repo.saveSession(r.token, r.user)
                    AuthState.pendingToken = r.token
                    nav.navigate(Routes.SETUP_TOTP) { popUpTo(0) { inclusive = true } }
                } else Toast.makeText(ctx, r.message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }.onFailure { checking = false; Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show() }
        }
    }

    Column(Modifier.fillMaxSize().background(NexaBg).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(NexaSurface2),
            contentAlignment = Alignment.Center) { Icon(Icons.Filled.Mail, null, tint = NexaGreen, modifier = Modifier.size(44.dp)) }
        Spacer(Modifier.height(28.dp))
        Text("Verify your email", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Spacer(Modifier.height(12.dp))
        Text("We sent a verification link to:\n$email\n\nOpen it, then tap Verify below.",
            color = NexaMuted, fontSize = 13.5.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        Spacer(Modifier.height(32.dp))
        GradientButton(text = if (checking) "Checking…" else "I've Verified — Continue",
            enabled = !checking, onClick = { check() })
        Spacer(Modifier.height(14.dp))
        Text(if (sending) "Sending…" else "Resend email", color = NexaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(NexaSurface)
                .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(10.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    if (sending) return@clickable
                    sending = true
                    scope.launch { repo.resendVerification(); delay(1200); sending = false
                        Toast.makeText(ctx, "Verification email sent", Toast.LENGTH_SHORT).show() }
                }.padding(horizontal = 16.dp, vertical = 10.dp))
        Spacer(Modifier.height(30.dp))
        Text("Wrong email? Start over", color = NexaDim, fontSize = 12.sp,
            modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                prefs.clear(); nav.navigate(Routes.SIGNUP) { popUpTo(0) { inclusive = true } }
            })
    }
}
