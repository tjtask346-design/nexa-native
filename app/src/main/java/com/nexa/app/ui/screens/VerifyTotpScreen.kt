package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VerifyTotpScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var lastSubmitted by remember { mutableStateOf("") }

    val email = AuthState.pendingEmail ?: prefs.email ?: ""
    val pin = prefs.pin ?: ""

    fun verify(otp: String) {
        if (busy) return
        scope.launch {
            busy = true; error = null
            repo.loginPin(email, pin, otp).onSuccess { r ->
                busy = false
                if (r.success && r.token != null) {
                    repo.saveSession(r.token, r.user)
                    AuthState.reset()
                    nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                } else {
                    error = r.message ?: "Invalid code"
                    code = ""
                    lastSubmitted = ""
                }
            }.onFailure {
                busy = false
                error = it.message ?: "Network error"
                code = ""
                lastSubmitted = ""
            }
        }
    }

    LaunchedEffect(code) {
        if (code.length == 6 && code != lastSubmitted && !busy) {
            lastSubmitted = code
            verify(code)
        }
    }

    Column(Modifier.fillMaxSize().background(NexaBg).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(22.dp))
        NexaIconButton(onClick = {
            AuthState.reset()
            nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }) {
            Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Box(
                Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha = 0.15f), NexaTeal.copy(alpha = 0.12f))))
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Lock, null, tint = NexaGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                "Two-Factor Code",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Open your authenticator app\nand enter the 6-digit code.",
                color = NexaMuted, fontSize = 13.sp,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )
            Spacer(Modifier.height(28.dp))
            CodeInput6(value = code) { code = it.filter { c -> c.isDigit() }.take(6) }
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(error!!, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
            if (busy) {
                Spacer(Modifier.height(12.dp))
                Text("Verifying…", color = NexaGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}
