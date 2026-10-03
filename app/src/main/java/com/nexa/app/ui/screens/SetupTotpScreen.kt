package com.nexa.app.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SetupTotpScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var loading by remember { mutableStateOf(true) }
    var secret by remember { mutableStateOf("") }
    var otpauth by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var lastSubmitted by remember { mutableStateOf("") }
    val token = AuthState.pendingToken ?: prefs.token

    LaunchedEffect(Unit) {
        if (token.isNullOrBlank()) {
            error = "Session expired. Please login again."
            loading = false
            return@LaunchedEffect
        }
        repo.setupTotp(token).onSuccess {
            if (it.success && it.secret != null) {
                secret = it.secret
                otpauth = it.otpauth ?: ""
            } else {
                error = it.message ?: "Setup failed"
            }
            loading = false
        }.onFailure {
            error = it.message ?: "Network error"
            loading = false
        }
    }

    val qr: Bitmap? = remember(otpauth) {
        if (otpauth.isBlank()) null
        else try {
            val size = 512
            val bits = QRCodeWriter().encode(otpauth, BarcodeFormat.QR_CODE, size, size)
            val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) for (y in 0 until size)
                b.setPixel(
                    x, y,
                    if (bits[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            b
        } catch (_: Exception) { null }
    }

    fun verify(otp: String) {
        if (busy || token.isNullOrBlank()) return
        scope.launch {
            busy = true
            error = null
            repo.verifyTotpSetup(token, otp).onSuccess { r ->
                busy = false
                if (r.success) {
                    AuthState.reset()
                    Toast.makeText(ctx, "2FA enabled ✓", Toast.LENGTH_SHORT).show()
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

    // Auto-verify when 6 digits entered or pasted
    LaunchedEffect(code) {
        if (code.length == 6 && code != lastSubmitted && !busy && !loading && error == null) {
            lastSubmitted = code
            verify(code)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NexaGreen.copy(alpha = 0.15f),
                                NexaTeal.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Shield,
                    null,
                    tint = NexaGreen,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Enable Two-Factor",
                color = NexaText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Scan with Google Authenticator or Authy",
                color = NexaMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            when {
                loading -> Text("Generating secret…", color = NexaMuted, fontSize = 13.sp)

                error != null && secret.isBlank() -> Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NexaRed.copy(alpha = 0.1f))
                        .border(1.dp, NexaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(error!!, color = NexaRed, fontSize = 12.5.sp)
                }

                else -> {
                    // QR Code
                    Box(
                        Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(14.dp)
                    ) {
                        if (qr != null) {
                            Image(qr.asImageBitmap(), "QR", modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Can't scan? Enter manually:", color = NexaDim, fontSize = 11.5.sp)
                    Spacer(Modifier.height(8.dp))

                    // Secret box
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexaSurface)
                            .border(
                                1.dp,
                                NexaBorder.copy(alpha = 0.09f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                secret,
                                color = NexaText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NexaGreen.copy(alpha = 0.13f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        clipboard.setText(AnnotatedString(secret))
                                        Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        null,
                                        tint = NexaGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "Copy",
                                        color = NexaGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                    Text(
                        "ENTER 6-DIGIT CODE",
                        color = NexaMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // ═══ NEW CodeInput6 with dialpad + paste ═══
                    CodeInput6(value = code) { code = it.filter { c -> c.isDigit() }.take(6) }

                    if (error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            error!!,
                            color = NexaRed,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (busy) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Verifying…",
                            color = NexaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    GradientButton(
                        text = "Verify & Enable",
                        enabled = code.length == 6 && !busy,
                        loading = busy,
                        onClick = { verify(code) }
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// CODE INPUT — 6 boxes + Paste button + Custom dialpad
// ═══════════════════════════════════════════════
@Composable
fun CodeInput6(value: String, onChange: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showKeypad by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        // ═══ 6 boxes ═══
        Row(
            Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(6) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NexaSurface)
                        .border(
                            1.5.dp,
                            if (i < value.length) NexaGreen.copy(alpha = 0.5f)
                            else NexaBorder.copy(alpha = 0.09f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showKeypad = !showKeypad },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (i < value.length) value[i].toString() else "",
                        color = NexaText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ═══ PASTE button ═══
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NexaGreen.copy(alpha = 0.12f))
                .border(
                    1.dp,
                    NexaGreen.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val clip = clipboard.getText()?.text ?: ""
                    val digits = clip.filter { it.isDigit() }.take(6)
                    if (digits.isNotEmpty()) {
                        onChange(digits)
                        showKeypad = false
                    } else {
                        Toast.makeText(
                            context,
                            "No digits in clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.ContentPaste,
                    contentDescription = null,
                    tint = NexaGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Paste from clipboard",
                    color = NexaGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // ═══ Custom numeric keypad ═══
        if (showKeypad) {
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Rows 1-9
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
                rows.forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { digit ->
                            KeypadButton(
                                digit = digit,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (value.length < 6) onChange(value + digit)
                                }
                            )
                        }
                    }
                }

                // Row: Backspace / 0 / Done
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Backspace
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexaSurface2)
                            .border(
                                1.dp,
                                NexaBorder.copy(alpha = 0.15f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (value.isNotEmpty()) onChange(value.dropLast(1))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = NexaMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Zero
                    KeypadButton(
                        digit = "0",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (value.length < 6) onChange(value + "0")
                        }
                    )

                    // Done
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (value.length == 6) NexaGreen.copy(alpha = 0.2f)
                                else NexaSurface2
                            )
                            .border(
                                1.dp,
                                if (value.length == 6) NexaGreen.copy(alpha = 0.5f)
                                else NexaBorder.copy(alpha = 0.15f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showKeypad = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Done",
                            color = if (value.length == 6) NexaGreen else NexaMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// KEYPAD BUTTON (helper for digits 0-9)
// ═══════════════════════════════════════════════
@Composable
private fun KeypadButton(
    digit: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NexaSurface)
            .border(
                1.dp,
                NexaBorder.copy(alpha = 0.15f),
                RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            digit,
            color = NexaText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
