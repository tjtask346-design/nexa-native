package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    var step by remember { mutableIntStateOf(1) } // 1 = verify TOTP, 2 = new PIN, 3 = confirm PIN
    var totpCode by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var pinBuffer by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val email = AuthState.pendingEmail ?: prefs.email ?: ""

    Column(Modifier.fillMaxSize().background(NexaBg).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(22.dp))

        NexaIconButton(onClick = {
            if (step > 1) { step = 1; pinBuffer = ""; error = null }
            else nav.popBackStack()
        }) {
            Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        when (step) {
            1 -> TotpStep(
                code = totpCode,
                onChange = { totpCode = it.filter { c -> c.isDigit() }.take(6) },
                error = error,
                onNext = {
                    if (totpCode.length == 6) {
                        step = 2
                        error = null
                    }
                }
            )
            2 -> PinStep(
                title = "Create new PIN",
                subtitle = "Choose a 5-digit PIN",
                buffer = pinBuffer,
                error = error,
                onDigit = { d ->
                    if (pinBuffer.length < 5) {
                        pinBuffer += d
                        if (pinBuffer.length == 5) {
                            firstPin = pinBuffer
                            pinBuffer = ""
                            step = 3
                            error = null
                        }
                    }
                },
                onDelete = { pinBuffer = pinBuffer.dropLast(1) }
            )
            3 -> PinStep(
                title = "Confirm new PIN",
                subtitle = "Enter the same PIN again",
                buffer = pinBuffer,
                error = error,
                onDigit = { d ->
                    if (pinBuffer.length < 5) {
                        pinBuffer += d
                        if (pinBuffer.length == 5) {
                            if (pinBuffer == firstPin) {
                                scope.launch {
                                    busy = true
                                    val res = repo.resetPinWithTotp(email, totpCode, firstPin)
                                    busy = false
                                    res.onSuccess { r ->
                                        if (r.success) {
                                            Toast.makeText(ctx, "PIN reset ✓", Toast.LENGTH_SHORT).show()
                                            AuthState.reset()
                                            nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                        } else {
                                            error = r.message ?: "Reset failed"
                                            pinBuffer = ""
                                            step = 1
                                        }
                                    }.onFailure {
                                        error = it.message ?: "Network error"
                                        pinBuffer = ""
                                        step = 1
                                    }
                                }
                            } else {
                                error = "PINs do not match"
                                pinBuffer = ""
                                firstPin = ""
                                step = 2
                            }
                        }
                    }
                },
                onDelete = { pinBuffer = pinBuffer.dropLast(1) },
                busy = busy
            )
        }
    }
}

@Composable
private fun TotpStep(
    code: String,
    onChange: (String) -> Unit,
    error: String?,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))

        Box(
            Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha = 0.15f), NexaTeal.copy(alpha = 0.12f))))
                .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(42.dp))
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "Reset your PIN",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Enter the 6-digit code from your\nauthenticator app to verify it's you.",
            color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 19.sp
        )

        Spacer(Modifier.height(32.dp))

        CodeInput6(value = code, onChange = onChange)

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = "Continue",
            enabled = code.length == 6,
            onClick = onNext,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun PinStep(
    title: String,
    subtitle: String,
    buffer: String,
    error: String?,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    busy: Boolean = false
) {
    Column(
        Modifier.fillMaxSize(),
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

        Text(title, color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(40.dp))

        PinDots(filled = buffer.length, error = error != null)

        Spacer(Modifier.height(14.dp))
        Text(
            text = error ?: if (busy) "Please wait…" else "",
            color = if (error != null) NexaRed else NexaMuted,
            fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().height(20.dp)
        )

        Spacer(Modifier.weight(1f))

        PinKeypad(
            onDigit = onDigit,
            onDelete = onDelete,
            onFingerprint = null,
            fingerprintEnabled = false,
            modifier = Modifier.padding(bottom = 28.dp)
        )
    }
}
