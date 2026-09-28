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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.nexa.app.ui.components.NexaIconButton
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

    val token = AuthState.pendingToken ?: prefs.token

    LaunchedEffect(Unit) {
        if (token.isNullOrBlank()) {
            error = "Session expired. Please login again."
            loading = false
            return@LaunchedEffect
        }
        repo.setupTotp(token).onSuccess { r ->
            if (r.success && r.secret != null) {
                secret = r.secret
                otpauth = r.otpauth ?: ""
            } else {
                error = r.message ?: "Setup failed"
            }
            loading = false
        }.onFailure {
            error = it.message ?: "Network error"
            loading = false
        }
    }

    val qrBitmap: Bitmap? = remember(otpauth) {
        if (otpauth.isBlank()) null
        else try {
            val size = 512
            val bits = QRCodeWriter().encode(otpauth, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
            bmp
        } catch (_: Exception) { null }
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
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Box(
                Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha = 0.15f), NexaTeal.copy(alpha = 0.12f))))
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(42.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Enable Two-Factor",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Scan this QR with Google Authenticator\nor Authy to secure your account.",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )

            Spacer(Modifier.height(24.dp))

            // Loading / Error
            when {
                loading -> {
                    Text("Generating secret…", color = NexaMuted, fontSize = 13.sp)
                }
                error != null -> {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(NexaRed.copy(alpha = 0.1f))
                            .border(1.dp, NexaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(error!!, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                else -> {
                    // QR
                    Box(
                        Modifier.size(220.dp).clip(RoundedCornerShape(20.dp))
                            .background(Color.White).padding(14.dp)
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Secret manual entry
                    Text(
                        "Can't scan? Enter this code manually:",
                        color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(NexaSurface)
                            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                secret,
                                color = NexaText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                            Box(
                                Modifier.clip(RoundedCornerShape(10.dp))
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
                                    Icon(Icons.Filled.ContentCopy, null, tint = NexaGreen, modifier = Modifier.size(12.dp))
                                    Text("Copy", color = NexaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // 6-digit input
                    Text(
                        "ENTER 6-DIGIT CODE FROM APP",
                        color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    CodeInput6(
                        value = code,
                        onChange = { code = it.filter { c -> c.isDigit() }.take(6) }
                    )

                    Spacer(Modifier.height(28.dp))

                    GradientButton(
                        text = "Verify & Enable",
                        enabled = code.length == 6 && !busy,
                        loading = busy,
                        onClick = {
                            scope.launch {
                                busy = true
                                val res = repo.verifyTotpSetup(token!!, code)
                                busy = false
                                res.onSuccess { r ->
                                    if (r.success) {
                                        AuthState.reset()
                                        Toast.makeText(ctx, "2FA enabled ✓", Toast.LENGTH_SHORT).show()
                                        nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                                    } else {
                                        error = r.message ?: "Invalid code"
                                        code = ""
                                    }
                                }.onFailure {
                                    error = it.message ?: "Network error"
                                    code = ""
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun CodeInput6(value: String, onChange: (String) -> Unit) {
    val focused = remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focused.value = true }
    ) {
        if (!focused.value) {
            // Show visual boxes
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexaSurface)
                            .border(
                                1.5.dp,
                                if (i < value.length) NexaGreen.copy(alpha = 0.5f)
                                else NexaBorder.copy(alpha = 0.09f),
                                RoundedCornerShape(14.dp)
                            ),
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
        }

        // Invisible input capturing focus
        BasicTextField(
            value = value,
            onValueChange = onChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .onFocusChanged { focused.value = it.isFocused }
        )
    }
}
