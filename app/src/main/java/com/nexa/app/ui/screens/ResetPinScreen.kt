package com.nexa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShapeimport android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.PinDots
import com.nexa.app.ui.components.PinKeypad
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ResetPinScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(1) }
    var totpCode by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var buf by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val email = AuthState.pendingEmail ?: prefs.email ?: ""

    // ⚡ Auto-advance to step 2 when 6 digits are entered/pasted
    LaunchedEffect(totpCode) {
        if (totpCode.length == 6 && step == 1) {
            error = null
            step = 2
        }
    }

    Column(Modifier.fillMaxSize().background(NexaBg).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(22.dp))
        NexaIconButton(onClick = {
            if (step > 1) { step = 1; buf = ""; error = null; totpCode = "" } else nav.popBackStack()
        }) { Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        when (step) {
            1 -> {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(30.dp))
                    Box(Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha=0.15f), NexaTeal.copy(alpha=0.12f))))
                        .border(1.dp, NexaGreen.copy(alpha=0.3f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(42.dp))
                    }
                    Spacer(Modifier.height(22.dp))
                    Text("Reset your PIN", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Enter 6-digit code from your\nauthenticator app.", color = NexaMuted,
                        fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 19.sp)
                    Spacer(Modifier.height(32.dp))
                    CodeInput6(value = totpCode) { totpCode = it.filter { c -> c.isDigit() }.take(6) }
                    if (error != null) { Spacer(Modifier.height(16.dp))
                        Text(error!!, color = NexaRed, fontSize = 12.5.sp) }
                }
                GradientButton(text = "Continue", enabled = totpCode.length == 6,
                    onClick = { step = 2; error = null }, modifier = Modifier.padding(bottom = 32.dp))
            }
            2, 3 -> {
                val title = if (step == 2) "Create new PIN" else "Confirm new PIN"
                val sub = if (step == 2) "Choose 5-digit PIN" else "Enter same PIN again"
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(30.dp))
                    Box(Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha=0.15f), NexaTeal.copy(alpha=0.12f))))
                        .border(1.dp, NexaGreen.copy(alpha=0.3f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Lock, null, tint = NexaGreen, modifier = Modifier.size(40.dp))
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(title, color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(sub, color = NexaMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(40.dp))
                    PinDots(filled = buf.length, error = error != null)
                    Spacer(Modifier.height(14.dp))
                    Text(if (error != null) error!! else if (busy) "Please wait…" else "",
                        color = if (error != null) NexaRed else NexaMuted, fontSize = 12.5.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().height(20.dp))
                    Spacer(Modifier.weight(1f))
                    PinKeypad(
                        onDigit = { d ->
                            if (buf.length < 5) {
                                buf += d
                                if (buf.length == 5) {
                                    if (step == 2) { firstPin = buf; buf = ""; step = 3; error = null }
                                    else if (buf == firstPin) {
                                        scope.launch {
                                            busy = true
                                            repo.resetPinWithTotp(email, totpCode, firstPin).onSuccess { r ->
                                                busy = false
                                                if (r.success) { Toast.makeText(ctx, "PIN reset ✓", Toast.LENGTH_SHORT).show()
                                                    AuthState.reset(); nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
                                                else { error = r.message ?: "Reset failed"; buf = ""; step = 1; totpCode = "" }
                                            }.onFailure { busy = false; error = it.message ?: "Network error"; buf = ""; step = 1; totpCode = "" }
                                        }
                                    } else { error = "PINs do not match"; buf = ""; firstPin = ""; step = 2 }
                                }
                            }
                        },
                        onDelete = { buf = buf.dropLast(1) },
                        onFingerprint = null,
                        fingerprintEnabled = false,
                        modifier = Modifier.padding(bottom = 28.dp))
                }
            }
        }
    }
}
