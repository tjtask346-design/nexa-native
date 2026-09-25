package com.nexa.app.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.PinDots
import com.nexa.app.ui.components.PinKeypad
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PinScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = ctx as? FragmentActivity

    var mode by remember { mutableStateOf(AuthState.pinMode.ifEmpty { "login" }) }
    var firstPin by remember { mutableStateOf("") }
    var buffer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun submit(pin: String) {
        scope.launch {
            busy = true
            error = null
            when (mode) {
                "setup", "confirm" -> {
                    val email = AuthState.pendingEmail ?: prefs.email ?: ""
                    val fullName = AuthState.pendingName ?: prefs.name ?: email.substringBefore("@")
                    val res = repo.register(email, fullName, pin)
                    res.onSuccess { r ->
                        if (r.success && r.token != null) {
                            repo.saveSession(r.token, r.user)
                            repo.savePin(pin)
                            AuthState.reset()
                            nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                        } else {
                            error = r.message ?: "Registration failed"
                            buffer = ""
                        }
                    }.onFailure {
                        error = it.message ?: "Network error"
                        buffer = ""
                    }
                }
                "login" -> {
                    val email = AuthState.pendingEmail ?: prefs.email ?: ""
                    val res = repo.loginPin(email, pin)
                    res.onSuccess { r ->
                        if (r.success && r.token != null) {
                            repo.saveSession(r.token, r.user)
                            repo.savePin(pin)
                            AuthState.reset()
                            nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                        } else {
                            error = r.message ?: "Login failed"
                            buffer = ""
                        }
                    }.onFailure {
                        error = it.message ?: "Network error"
                        buffer = ""
                    }
                }
            }
            busy = false
        }
    }

    fun onDigit(d: String) {
        if (buffer.length >= 5 || busy) return
        buffer += d
        error = null
        if (buffer.length == 5) {
            val pin = buffer
            when (mode) {
                "setup" -> { firstPin = pin; mode = "confirm"; buffer = "" }
                "confirm" -> {
                    if (pin == firstPin) submit(pin)
                    else { error = "PINs do not match"; buffer = ""; mode = "setup"; firstPin = "" }
                }
                else -> submit(pin)
            }
        }
    }

    fun launchFingerprint() {
        if (activity == null) { error = "Biometric unavailable"; return }
        val mgr = BiometricManager.from(ctx)
        val can = mgr.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (can != BiometricManager.BIOMETRIC_SUCCESS) { error = "No fingerprint enrolled"; return }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(ctx),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val saved = prefs.pin
                    if (saved != null) { buffer = saved; submit(saved) }
                    else error = "Type your PIN once to enable fingerprint"
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Nexa")
                .setSubtitle("Place your finger")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    Box(Modifier.fillMaxSize().background(NexaBg).padding(24.dp)) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text(
                when (mode) {
                    "setup" -> "Create your PIN"
                    "confirm" -> "Confirm your PIN"
                    else -> "Enter your PIN"
                },
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                when (mode) {
                    "setup" -> "Choose a 5-digit PIN"
                    "confirm" -> "Enter the same PIN again"
                    else -> "Enter your 5-digit PIN to unlock"
                },
                color = NexaMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(44.dp))
            PinDots(filled = buffer.length, error = error != null)
            Spacer(Modifier.height(14.dp))
            if (error != null) {
                Text(error!!, color = NexaRed, fontSize = 12.sp, textAlign = TextAlign.Center)
            } else if (busy) {
                Text("Please wait…", color = NexaMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            PinKeypad(
                onDigit = ::onDigit,
                onDelete = { buffer = buffer.dropLast(1); error = null },
                onFingerprint = if (mode == "login" && prefs.pin != null) ::launchFingerprint else null
            )
        }
    }
}
