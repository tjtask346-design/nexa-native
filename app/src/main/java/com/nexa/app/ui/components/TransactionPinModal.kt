package com.nexa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.nexa.app.data.BiometricHelper
import com.nexa.app.data.Prefs
import com.nexa.app.ui.animations.shake
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Full-screen PIN modal for confirming transactions.
 *
 * Either/or UX:
 *  - If a fingerprint is enrolled, we auto-launch the biometric prompt on open.
 *  - Cancel/fail → user can just type the PIN.
 *  - Typing PIN → auto-submits at 5 digits (no need for fingerprint).
 *  - Fingerprint success → auto-submits (no need for PIN).
 */
@Composable
fun TransactionPinModal(
    visible: Boolean,
    title: String,
    subtitle: String,
    amount: String,
    prefs: Prefs,
    expectedPin: String?,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    if (!visible) return

    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity
    var buffer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var fpActive by remember { mutableStateOf(false) }
    var fpLaunchAttempted by remember { mutableStateOf(false) }

    val fingerprintAvailable = remember { BiometricHelper.isAvailable(ctx) }
    val canUseFingerprint = prefs.pin != null && activity != null && fingerprintAvailable

    // Reset when opened
    LaunchedEffect(visible) {
        buffer = ""
        error = null
        fpActive = false
        fpLaunchAttempted = false
    }

    fun submitPin(pin: String) {
        if (expectedPin == null || pin == expectedPin) {
            onConfirmed()
        } else {
            error = "Incorrect PIN"
            shakeTrigger++
            buffer = ""
        }
    }

    fun onDigit(d: String) {
        if (buffer.length >= 5) return
        buffer += d
        error = null
        // Auto-submit when 5 digits typed — no fingerprint needed
        if (buffer.length == 5) submitPin(buffer)
    }

    fun launchFingerprint() {
        if (!canUseFingerprint || activity == null) return
        fpActive = true
        error = null
        BiometricHelper.promptForPin(
            activity = activity,
            prefs = prefs,
            title = "Confirm with fingerprint",
            subtitle = title,
            onPin = { pin ->
                fpActive = false
                // Fingerprint matched → submit immediately, no PIN typing needed
                submitPin(pin)
            },
            onError = { msg ->
                fpActive = false
                // Cancel/fail → just ignore, user can type PIN instead
                if (!msg.contains("cancel", true) && !msg.contains("Cancel", true)) {
                    error = null // silent — user can still type
                }
            }
        )
    }

    // Auto-launch fingerprint once when modal opens (either/or UX)
    LaunchedEffect(visible, canUseFingerprint) {
        if (visible && canUseFingerprint && !fpLaunchAttempted) {
            fpLaunchAttempted = true
            delay(300) // small delay for the modal to appear first
            launchFingerprint()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xF7020604))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            // Close button
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NexaSurface2)
                        .border(1.dp, NexaBorder.copy(alpha = 0.09f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = NexaText, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(30.dp))

            // Amount + title + subtitle
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    amount,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.3).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal))
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text(title, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = NexaMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            // Dots + error
            Box(Modifier.fillMaxWidth().shake(shakeTrigger), contentAlignment = Alignment.Center) {
                PinDots(filled = buffer.length, error = error != null)
            }
            Spacer(Modifier.height(12.dp))

            // Status hint — tells the user clearly: either/or
            val hint = when {
                error != null -> error!!
                fpActive -> "Waiting for fingerprint…"
                canUseFingerprint -> "Use fingerprint or type your PIN"
                else -> "Type your 5-digit PIN"
            }
            Text(
                text = hint,
                color = when {
                    error != null -> NexaRed
                    fpActive -> NexaGreen
                    else -> NexaDim
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().height(20.dp)
            )

            Spacer(Modifier.weight(1f))

            // Keypad with fingerprint always visible if enrolled
            PinKeypad(
                onDigit = ::onDigit,
                onDelete = { buffer = buffer.dropLast(1); error = null },
                onFingerprint = ::launchFingerprint,
                fingerprintEnabled = canUseFingerprint,
                fingerprintActive = fpActive,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}
