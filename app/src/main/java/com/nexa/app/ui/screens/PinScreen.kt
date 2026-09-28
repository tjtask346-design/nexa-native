package com.nexa.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.AuthState
import com.nexa.app.data.BiometricHelper
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.animations.fadeUp
import com.nexa.app.ui.animations.shake
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.PinDots
import com.nexa.app.ui.components.PinKeypad
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = ctx as? FragmentActivity

    var mode by remember { mutableStateOf(AuthState.pinMode.ifEmpty { "login" }) }
    var firstPin by remember { mutableStateOf("") }
    var buf by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var shake by remember { mutableIntStateOf(0) }
    var fpActive by remember { mutableStateOf(false) }
    var fpTried by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }

    val enrolled = remember { BiometricHelper.hasEnrolledBiometric(ctx) }
    val showFp = mode == "login" && prefs.pin != null && enrolled && prefs.biometricEnabled && activity != null

    fun goVerifyEmail() { nav.navigate(Routes.VERIFY_EMAIL) { popUpTo(Routes.PIN) { inclusive = true } } }

    fun submitSignup(pin: String) {
        scope.launch {
            busy = true; error = null
            val email = AuthState.pendingEmail ?: prefs.email ?: ""
            val res = repo.createFirebaseUser(email)
            if (res.isFailure) {
                error = res.exceptionOrNull()?.message ?: "Firebase error"
                shake++; buf = ""; busy = false; return@launch
            }
            repo.savePin(pin); busy = false
            if (enrolled && activity != null) showBioDialog = true else goVerifyEmail()
        }
    }

    fun handleLogin(r: com.nexa.app.data.AuthResponse) {
        when {
            r.requiresTotp -> {
                prefs.pin = buf
                AuthState.pendingEmail = AuthState.pendingEmail ?: prefs.email
                nav.navigate(Routes.VERIFY_TOTP) { popUpTo(Routes.PIN) { inclusive = true } }
            }
            r.requiresTotpSetup && r.token != null -> {
                AuthState.pendingToken = r.token
                repo.saveSession(r.token, r.user); repo.savePin(buf)
                nav.navigate(Routes.SETUP_TOTP) { popUpTo(Routes.PIN) { inclusive = true } }
            }
            r.success && r.token != null -> {
                repo.saveSession(r.token, r.user); repo.savePin(buf)
                AuthState.reset()
                nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
            }
            else -> { error = r.message ?: "Login failed"; shake++; buf = "" }
        }
    }

    fun submitLogin(pin: String) {
        scope.launch {
            busy = true
            error = null

            // ═══════════════════════════════════════
            // SMART SESSION RESTORE
            // If a valid token exists locally AND PIN matches,
            // unlock directly without backend call or TOTP.
            // (Only explicit LOGOUT should require TOTP again.)
            // ═══════════════════════════════════════
            val hasToken = !prefs.token.isNullOrBlank()
            val savedPin = prefs.pin
            if (hasToken && savedPin != null && pin == savedPin) {
                busy = false
                AuthState.reset()
                nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                return@launch
            }

            // Otherwise → full backend login (TOTP required if enabled)
            val email = AuthState.pendingEmail ?: prefs.email ?: ""
            repo.loginPin(email, pin).onSuccess { handleLogin(it) }
                .onFailure { error = it.message ?: "Network error"; shake++; buf = "" }
            busy = false
        }
    }

    fun onDigit(d: String) {
        if (buf.length >= 5 || busy) return
        buf += d; error = null
        if (buf.length == 5) {
            val p = buf
            when (mode) {
                "setup" -> { firstPin = p; mode = "confirm"; buf = "" }
                "confirm" -> if (p == firstPin) submitSignup(p) else {
                    error = "PINs do not match"; shake++; buf = ""; mode = "setup"; firstPin = ""
                }
                else -> submitLogin(p)
            }
        }
    }

    fun launchFp() {
        if (!showFp || activity == null) return
        fpActive = true
        BiometricHelper.promptForPin(
            activity, prefs, "Unlock Nexa", "Place your finger",
            onPin = { p -> fpActive = false; submitLogin(p) },
            onError = { fpActive = false }
        )
    }

    LaunchedEffect(mode, showFp) {
        if (mode == "login" && showFp && !fpTried) { fpTried = true; delay(400); launchFp() }
        if (mode != "login") fpTried = false
    }

    Column(Modifier.fillMaxSize().background(NexaBg).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(22.dp))
        NexaIconButton(onClick = {
            if (mode == "confirm") { mode = "setup"; firstPin = ""; buf = "" }
            else { AuthState.reset(); nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
        }) { Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(30.dp))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).fadeUp(0)) {
                Image(painterResource(R.drawable.nexa_logo), null, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(20.dp))
            Text(
                when (mode) {
                    "setup" -> "Create your PIN"
                    "confirm" -> "Confirm your PIN"
                    else -> "Enter your PIN"
                },
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                modifier = Modifier.fadeUp(100)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                when (mode) {
                    "setup" -> "Choose 5-digit PIN"
                    "confirm" -> "Same PIN again"
                    else -> "Enter 5-digit PIN to unlock"
                },
                color = NexaMuted, fontSize = 13.sp, modifier = Modifier.fadeUp(150)
            )
        }

        Spacer(Modifier.height(44.dp))

        Box(Modifier.fillMaxWidth().shake(shake), contentAlignment = Alignment.Center) {
            PinDots(filled = buf.length, error = error != null)
        }

        Spacer(Modifier.height(14.dp))
        Text(
            if (error != null) error!! else if (busy) "Please wait…" else "",
            color = if (error != null) NexaRed else NexaMuted, fontSize = 12.5.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().height(20.dp)
        )

        Spacer(Modifier.weight(1f))

        PinKeypad(
            onDigit = ::onDigit,
            onDelete = { buf = buf.dropLast(1); error = null },
            onFingerprint = ::launchFp,
            fingerprintEnabled = showFp,
            fingerprintActive = fpActive,
            modifier = Modifier.padding(bottom = 28.dp)
        )
    }

    if (showBioDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = NexaSurface,
            titleContentColor = NexaText,
            textContentColor = NexaMuted,
            title = { Text("Enable Fingerprint?", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text = { Text("Setup fingerprint for faster login next time.", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showBioDialog = false
                    if (activity != null) {
                        BiometricHelper.prompt(
                            activity, "Enable Fingerprint", "Place finger",
                            onSuccess = { prefs.biometricEnabled = true; goVerifyEmail() },
                            onError = { goVerifyEmail() }
                        )
                    } else goVerifyEmail()
                }) { Text("Enable", color = NexaGreen, fontWeight = FontWeight.ExtraBold) }
            },
            dismissButton = {
                TextButton(onClick = { showBioDialog = false; goVerifyEmail() }) {
                    Text("Not Now", color = NexaMuted)
                }
            }
        )
    }
}
