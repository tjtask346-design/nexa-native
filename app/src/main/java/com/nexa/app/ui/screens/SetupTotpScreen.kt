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
                b.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(36.dp))

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NexaGreen.copy(alpha = 0.16f),
                                NexaTeal.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, NexaGreen.copy(alpha = 0.28f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Enable Two-Factor",
                color = NexaText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                letterSpacing = (-0.4).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Scan the QR with Google Authenticator",
                color = NexaMuted, fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        when {
            loading -> {
                Box(
                    Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Generating secret…", color = NexaMuted, fontSize = 13.sp)
                }
            }

            error != null && secret.isBlank() -> {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(NexaRed.copy(alpha = 0.1f))
                        .border(1.dp, NexaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(error!!, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
            }

            else -> {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        if (qr != null) {
                            Image(qr.asImageBitmap(), "QR", modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Can't scan? Enter manually",
                        color = NexaDim, fontSize = 11.sp, fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexaSurface)
                            .border(1.dp, NexaBorder.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                secret,
                                color = NexaText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(NexaGreen.copy(alpha = 0.14f))
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
                                    Icon(Icons.Filled.ContentCopy, null, tint = NexaGreen, modifier = Modifier.size(12.dp))
                                    Text("Copy", color = NexaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "ENTER 6-DIGIT CODE",
                    color = NexaMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(14.dp))

                // ═══ CodeInput6 call — trailing lambda goes to onChange now ═══
                CodeInput6(value = code) { code = it.filter { c -> c.isDigit() }.take(6) }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error!!,
                        color = NexaRed, fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                if (busy) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Verifying…",
                        color = NexaGreen, fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
// SHARED CODE INPUT
// ⚠️ IMPORTANT: onChange is the LAST parameter so trailing lambda works
// ═══════════════════════════════════════════════
@Composable
fun CodeInput6(
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 6 digit boxes
        Row(
            Modifier.widthIn(max = 340.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            repeat(6) { i ->
                val filled = i < value.length
                val active = i == value.length
                Box(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (active) NexaSurface2 else NexaSurface)
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = when {
                                active -> NexaGreen.copy(alpha = 0.65f)
                                filled -> NexaGreen.copy(alpha = 0.35f)
                                else -> NexaBorder.copy(alpha = 0.12f)
                            },
                            shape = RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (filled) {
                        Text(
                            value[i].toString(),
                            color = NexaText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else if (active) {
                        Box(
                            Modifier
                                .size(width = 2.dp, height = 20.dp)
                                .background(NexaGreen, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Dialpad
        Column(
            Modifier.widthIn(max = 300.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialKey("1", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "1") }
                DialKey("2", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "2") }
                DialKey("3", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "3") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialKey("4", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "4") }
                DialKey("5", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "5") }
                DialKey("6", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "6") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialKey("7", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "7") }
                DialKey("8", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "8") }
                DialKey("9", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "9") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionKey(Modifier.weight(1f), onClick = {
                    val clip = clipboard.getText()?.text ?: ""
                    val digits = clip.filter { it.isDigit() }.take(6)
                    if (digits.isNotEmpty()) onChange(digits)
                    else Toast.makeText(context, "No digits in clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.ContentPaste, "Paste", tint = NexaGreen, modifier = Modifier.size(22.dp))
                }
                DialKey("0", Modifier.weight(1f)) { if (value.length < 6) onChange(value + "0") }
                ActionKey(Modifier.weight(1f), onClick = {
                    if (value.isNotEmpty()) onChange(value.dropLast(1))
                }) {
                    Icon(Icons.Filled.Backspace, "Backspace", tint = NexaMuted, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun DialKey(
    digit: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .height(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            digit,
            color = NexaText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.3).sp
        )
    }
}

@Composable
private fun ActionKey(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .height(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NexaSurface2)
            .border(1.dp, NexaBorder.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
