package com.nexa.app.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*

// ═══════════════════════════════════════════════
// LOCAL ALIASES
// ═══════════════════════════════════════════════
private val PLACEHOLDER = Color(0xFF3B574A)
private val BORDER_GREEN_09 = Color(0x174ADE80)
private val INK = Color(0xFF04140D)
private val GRAD_COLORS = listOf(NexaGreen, NexaGreenDark, NexaTeal)

// ═══════════════════════════════════════════════
// AUTH BACKGROUND — 2 fixed radial glows
// ═══════════════════════════════════════════════
@Composable
private fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .drawBehind {
                // Glow A — top-left green
                val aRadius = 175.dp.toPx()
                val aCenter = Offset(90.dp.toPx(), (-10).dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaGreen.copy(alpha = 0.22f),
                            0.50f to NexaGreen.copy(alpha = 0.10f),
                            0.70f to NexaGreen.copy(alpha = 0.04f),
                            1.00f to Color.Transparent
                        ),
                        center = aCenter,
                        radius = aRadius
                    ),
                    radius = aRadius,
                    center = aCenter
                )

                // Glow B — right teal
                val bRadius = 160.dp.toPx()
                val bCenter = Offset(size.width - 10.dp.toPx(), 290.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to NexaTeal.copy(alpha = 0.16f),
                            0.50f to NexaTeal.copy(alpha = 0.07f),
                            0.70f to NexaTeal.copy(alpha = 0.03f),
                            1.00f to Color.Transparent
                        ),
                        center = bCenter,
                        radius = bRadius
                    ),
                    radius = bRadius,
                    center = bCenter
                )
            },
        content = content
    )
}

// ═══════════════════════════════════════════════
// LOGO WITH HALO — FIXED (renamed param)
// ═══════════════════════════════════════════════
@Composable
private fun LogoWithHalo(
    logoSize: Dp,
    cornerRadius: Dp
) {
    Box(
        Modifier.size(logoSize + 80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Halo behind (uses DrawScope's size, not param)
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val radius = this.size.minDimension / 2f
                    val c = this.center
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to NexaGreen.copy(alpha = 0.45f),
                                0.20f to NexaGreen.copy(alpha = 0.30f),
                                0.40f to NexaGreen.copy(alpha = 0.16f),
                                0.60f to NexaGreen.copy(alpha = 0.07f),
                                0.80f to NexaGreen.copy(alpha = 0.02f),
                                1.00f to Color.Transparent
                            ),
                            center = c,
                            radius = radius
                        ),
                        radius = radius,
                        center = c
                    )
                }
        )

        // Logo
        Box(Modifier.size(logoSize).clip(RoundedCornerShape(cornerRadius))) {
            Image(
                painter = painterResource(R.drawable.nexa_logo),
                contentDescription = "Nexa",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ═══════════════════════════════════════════════
// GRADIENT TEXT
// ═══════════════════════════════════════════════
@Composable
private fun GradientText(
    text: String,
    fontSize: TextUnit,
    letterSpacing: TextUnit
) {
    Text(
        text = text,
        style = TextStyle(
            brush = Brush.horizontalGradient(colors = GRAD_COLORS),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = letterSpacing
        )
    )
}

// ═══════════════════════════════════════════════
// INPUT FIELD
// ═══════════════════════════════════════════════
@Composable
private fun AuthInputField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    error: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var focused by remember { mutableStateOf(false) }

    val borderColor = when {
        error -> NexaRed.copy(alpha = 0.60f)
        focused -> NexaGreen.copy(alpha = 0.55f)
        else -> BORDER_GREEN_09
    }
    val bgColor = if (focused) NexaSurface2 else NexaSurface
    val ringColor = when {
        error -> NexaRed.copy(alpha = 0.10f)
        focused -> NexaGreen.copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        if (ringColor.alpha > 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding((-4).dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(ringColor)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(bgColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(17.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides
                        if (focused) NexaGreen else NexaDim
                ) { leadingIcon() }
            }
            Spacer(Modifier.width(11.dp))

            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = NexaText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.1).sp
                ),
                cursorBrush = SolidColor(NexaGreen),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focused = it.isFocused }
            ) { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 17.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            color = PLACEHOLDER,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    inner()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// NOTE ROW
// ═══════════════════════════════════════════════
@Composable
private fun AuthNoteRow(
    icon: @Composable () -> Unit,
    text: androidx.compose.ui.text.AnnotatedString
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            Modifier.size(14.dp).padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) { icon() }
        Text(
            text = text,
            color = NexaMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}

// ═══════════════════════════════════════════════
// GRADIENT BUTTON WITH SHINE + CORONA GLOW
// ═══════════════════════════════════════════════
@Composable
private fun AuthGradientButton(
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) 0.975f else 1f,
        label = "btnScale"
    )

    // Shine animation — 3.2s cycle, sweep in last 40%
    val transition = rememberInfiniteTransition(label = "shine")
    val shineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                0f at 0
                0f at 1920
                1f at 3200
            }
        ),
        label = "shine"
    )
    val shineX: Float = -0.6f + (1.9f * shineProgress)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        // Corona glow below button
        if (enabled && !loading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .drawBehind {
                        val radius = this.size.width * 0.65f
                        val center = Offset(this.size.width / 2f, this.size.height * 0.35f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.00f to NexaGreen.copy(alpha = 0.55f),
                                    0.20f to NexaGreen.copy(alpha = 0.38f),
                                    0.40f to NexaGreen.copy(alpha = 0.22f),
                                    0.60f to NexaGreen.copy(alpha = 0.10f),
                                    0.80f to NexaGreen.copy(alpha = 0.03f),
                                    1.00f to Color.Transparent
                                ),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                    }
            )
        }

        // Button
        Box(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (enabled && !loading) Brush.linearGradient(colors = GRAD_COLORS)
                    else Brush.linearGradient(colors = listOf(NexaDim, NexaDim))
                )
                .drawBehind {
                    if (enabled && !loading) {
                        val w = this.size.width
                        val h = this.size.height
                        val stripW = w * 0.4f
                        val startX = w * shineX
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.45f),
                                    Color.Transparent
                                ),
                                startX = startX,
                                endX = startX + stripW
                            ),
                            topLeft = Offset(startX, 0f),
                            size = Size(stripW, h)
                        )
                    }
                }
                .border(
                    width = 1.dp,
                    color = if (enabled && !loading) NexaGreen.copy(alpha = 0.30f)
                            else Color.Transparent,
                    shape = RoundedCornerShape(17.dp)
                )
                .clickable(
                    enabled = enabled && !loading,
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                )
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (loading) "Please wait…" else text,
                color = INK,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 0.1.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════
// SWITCH ROW
// ═══════════════════════════════════════════════
@Composable
private fun AuthSwitchRow(prefix: String, action: String, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prefix, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(
            action,
            color = NexaGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAction
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}

// ═══════════════════════════════════════════════
// DIVIDER
// ═══════════════════════════════════════════════
@Composable
private fun AuthDivider(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(BORDER_GREEN_09))
        Text(text, color = NexaDim, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Box(Modifier.weight(1f).height(1.dp).background(BORDER_GREEN_09))
    }
}

// ═══════════════════════════════════════════════
// TRUST CHIP
// ═══════════════════════════════════════════════
@Composable
private fun TrustChip(
    emoji: String,
    bold: String,
    normal: String,
    boldColor: Color = NexaGreen
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(NexaSurface)
            .border(1.dp, BORDER_GREEN_09, RoundedCornerShape(11.dp))
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 11.sp)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = boldColor, fontWeight = FontWeight.Bold)) { append(bold) }
                withStyle(SpanStyle(color = NexaMuted, fontWeight = FontWeight.Bold)) { append(normal) }
            },
            fontSize = 11.sp
        )
    }
}

// ═══════════════════════════════════════════════
// LOGIN SCREEN
// ═══════════════════════════════════════════════
@Composable
fun LoginScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf(prefs.email ?: "") }
    var emailError by remember { mutableStateOf(false) }
    val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

    AuthBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            LogoWithHalo(logoSize = 88.dp, cornerRadius = 24.dp)
            Spacer(Modifier.height(8.dp))
            GradientText(text = "NEXA", fontSize = 14.sp, letterSpacing = 6.sp)

            Spacer(Modifier.height(30.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Welcome ",
                    color = NexaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp
                )
                GradientText(text = "back", fontSize = 26.sp, letterSpacing = (-0.7).sp)
                Text(
                    " 👋",
                    color = NexaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Enter your email to continue.\nYou'll verify with your 5-digit PIN.",
                color = NexaMuted,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(30.dp))

            AuthInputField(
                value = email,
                onChange = { email = it; if (emailError) emailError = false },
                placeholder = "you@example.com",
                leadingIcon = {
                    Icon(
                        Icons.Filled.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                error = emailError,
                keyboardType = KeyboardType.Email
            )

            AuthNoteRow(
                icon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = NexaTeal,
                        modifier = Modifier.size(14.dp)
                    )
                },
                text = buildAnnotatedString {
                    append("Secured with a 5-digit PIN & fingerprint. No passwords to remember.")
                }
            )

            AuthGradientButton(
                text = "Continue with Email",
                enabled = email.isNotBlank(),
                onClick = {
                    val e = email.trim()
                    when {
                        e.isEmpty() -> emailError = true
                        !emailRegex.matches(e) -> emailError = true
                        else -> {
                            AuthState.pendingEmail = e
                            AuthState.pinMode = "login"
                            prefs.email = e
                            nav.navigate(Routes.PIN)
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            AuthSwitchRow(
                prefix = "Don't have an account? ",
                action = "Sign up"
            ) { nav.navigate(Routes.SIGNUP) }

            AuthDivider("SECURE & TRUSTED")

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                TrustChip("🔒", "256-bit", " encryption")
                TrustChip("👆", "Biometric", " ready")
                TrustChip("✅", "KYC", " verified")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ═══════════════════════════════════════════════
// SIGNUP SCREEN
// ═══════════════════════════════════════════════
@Composable
fun SignupScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

    AuthBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            LogoWithHalo(logoSize = 88.dp, cornerRadius = 24.dp)
            Spacer(Modifier.height(8.dp))
            GradientText(text = "NEXA", fontSize = 14.sp, letterSpacing = 6.sp)

            Spacer(Modifier.height(30.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Create your ",
                    color = NexaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp
                )
                GradientText(text = "account", fontSize = 26.sp, letterSpacing = (-0.7).sp)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Email-only registration — done in 30 seconds.\nThen set your secure 5-digit PIN.",
                color = NexaMuted,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            AuthInputField(
                value = email,
                onChange = { email = it; if (emailError) emailError = false },
                placeholder = "you@example.com",
                leadingIcon = {
                    Icon(
                        Icons.Filled.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                error = emailError,
                keyboardType = KeyboardType.Email
            )

            AuthInputField(
                value = name,
                onChange = { name = it },
                placeholder = "Full name",
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardType = KeyboardType.Text
            )

            AuthNoteRow(
                icon = {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = NexaTeal,
                        modifier = Modifier.size(14.dp)
                    )
                },
                text = buildAnnotatedString {
                    append("By signing up you agree to our ")
                    withStyle(SpanStyle(color = NexaTeal, fontWeight = FontWeight.Bold)) {
                        append("Terms")
                    }
                    append(" & ")
                    withStyle(SpanStyle(color = NexaTeal, fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy")
                    }
                    append(".")
                }
            )

            AuthGradientButton(
                text = "Create Free Account",
                enabled = email.isNotBlank() && name.isNotBlank(),
                onClick = {
                    val e = email.trim()
                    val n = name.trim()
                    when {
                        e.isEmpty() -> emailError = true
                        !emailRegex.matches(e) -> emailError = true
                        else -> {
                            AuthState.pendingEmail = e
                            AuthState.pendingName = n
                            AuthState.pinMode = "setup"
                            prefs.email = e
                            prefs.name = n
                            nav.navigate(Routes.PIN)
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            AuthSwitchRow(
                prefix = "Already have an account? ",
                action = "Log in"
            ) { nav.navigate(Routes.LOGIN) }

            AuthDivider("WHY NEXA")

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                TrustChip("⚡", "0% fee", " Nexa→Nexa")
                TrustChip("🌐", "LTC · USDT", "")
                TrustChip("📱", "bKash", "")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
