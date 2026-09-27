package com.nexa.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.BiometricHelper
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.components.NexaFabSheet
import com.nexa.app.ui.components.NexaSwitch
import com.nexa.app.ui.components.TransactionPinModal
import com.nexa.app.ui.theme.*

@Composable
fun ProfileScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity

    var showFabSheet by remember { mutableStateOf(false) }
    var biometricOn by remember { mutableStateOf(prefs.biometricEnabled) }

    // Fallback PIN verification (if biometric prompt fails)
    var pinVerifyMode by remember { mutableStateOf<String?>(null) } // "enable" | "disable" | null

    val hardwareAvailable = remember { BiometricHelper.isAvailable(ctx) }

    // Box wrapper so modal renders on TOP
    Box(Modifier.fillMaxSize()) {

        Column(Modifier.fillMaxSize().background(NexaBg)) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp)
            ) {
                Text(
                    "Profile",
                    color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 30.dp, bottom = 16.dp)
                )

                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(86.dp).clip(RoundedCornerShape(28.dp))
                            .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
                    ) {
                        Image(painterResource(R.drawable.nexa_logo), contentDescription = null, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(prefs.name ?: "User", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(prefs.email ?: "", color = NexaMuted, fontSize = 12.5.sp)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(9.dp))
                            .background(NexaTeal.copy(alpha = 0.11f))
                            .border(1.dp, NexaTeal.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Text("● ACTIVE", color = NexaTeal, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Column(
                    Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(20.dp)).background(NexaSurface)
                ) {
                    ProfileRow(Icons.Filled.QrCode, NexaGreen, "My QR Code", "Receive instant Nexa payments") {
                        nav.navigate(Routes.MYQR)
                    }

                    FingerprintRow(
                        icon = Icons.Filled.Fingerprint,
                        tint = if (biometricOn) NexaGreen else NexaTeal,
                        title = "Fingerprint",
                        subtitle = when {
                            !hardwareAvailable -> "Not available on this device"
                            biometricOn -> "Enabled for quick login"
                            else -> "Tap to enable quick login"
                        },
                        checked = biometricOn,
                        enabled = hardwareAvailable,
                        onToggle = { wantOn ->
                            if (!hardwareAvailable) {
                                Toast.makeText(ctx, "No fingerprint enrolled on this device", Toast.LENGTH_SHORT).show()
                                return@FingerprintRow
                            }

                            if (wantOn) {
                                // Enable flow: try biometric prompt first
                                if (activity != null) {
                                    BiometricHelper.prompt(
                                        activity = activity,
                                        title = "Enable fingerprint login",
                                        subtitle = "Place your finger to confirm",
                                        onSuccess = {
                                            prefs.biometricEnabled = true
                                            biometricOn = true
                                            Toast.makeText(ctx, "Fingerprint enabled ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { msg ->
                                            // If biometric fails (e.g. no fingerprint enrolled), fall back to PIN
                                            if (msg.contains("No biometric", true) ||
                                                msg.contains("not available", true) ||
                                                msg.contains("no fingerprint", true) ||
                                                msg.contains("No fingerprint", true)) {
                                                pinVerifyMode = "enable"
                                            } else {
                                                // User cancelled - just do nothing
                                                // Toast.makeText(ctx, "Cancelled", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                } else {
                                    pinVerifyMode = "enable"
                                }
                            } else {
                                // Disable flow: also verify with biometric
                                if (activity != null) {
                                    BiometricHelper.prompt(
                                        activity = activity,
                                        title = "Disable fingerprint",
                                        subtitle = "Place your finger to confirm",
                                        onSuccess = {
                                            prefs.biometricEnabled = false
                                            biometricOn = false
                                            Toast.makeText(ctx, "Fingerprint disabled", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            // User cancelled - keep enabled
                                        }
                                    )
                                } else {
                                    prefs.biometricEnabled = false
                                    biometricOn = false
                                }
                            }
                        }
                    )

                    ProfileRow(Icons.Filled.VerifiedUser, NexaGreen, "KYC Verification", "Complete your verification") {
                        nav.navigate(Routes.KYC)
                    }
                    ProfileRow(Icons.Filled.SupportAgent, NexaTeal, "Support", "24/7 live chat") { }
                    ProfileRow(Icons.AutoMirrored.Filled.Logout, NexaRed, "Log Out", "Sign out of your account") {
                        prefs.clear()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                }

                Text(
                    "Nexa v1.0.0",
                    color = NexaDim, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            NexaBottomBar(
                currentRoute = Routes.PROFILE,
                onNavigate = { nav.navigate(it) },
                onFabClick = { showFabSheet = true }
            )
        }

        // FAB sheet — on top of content
        if (showFabSheet) {
            NexaFabSheet(
                onDismiss = { showFabSheet = false },
                onNavigate = { nav.navigate(it) }
            )
        }

        // PIN fallback modal — LAST so it renders on TOP
        TransactionPinModal(
            visible = pinVerifyMode != null,
            title = if (pinVerifyMode == "enable") "Enable fingerprint" else "Disable fingerprint",
            subtitle = "Enter your PIN to confirm",
            amount = "",
            prefs = prefs,
            expectedPin = prefs.pin,
            onDismiss = { pinVerifyMode = null },
            onConfirmed = {
                val wasEnable = pinVerifyMode == "enable"
                pinVerifyMode = null
                prefs.biometricEnabled = wasEnable
                biometricOn = wasEnable
                Toast.makeText(
                    ctx,
                    if (wasEnable) "Fingerprint enabled ✓" else "Fingerprint disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val effectiveTint = if (enabled) tint else NexaDim
    Row(
        Modifier.fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                .background(effectiveTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = effectiveTint, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) NexaText else NexaMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            )
            Text(subtitle, color = NexaDim, fontSize = 11.sp)
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = NexaDim,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FingerprintRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val effectiveTint = if (enabled) tint else NexaDim
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                .background(effectiveTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = effectiveTint, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) NexaText else NexaMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            )
            Text(subtitle, color = NexaDim, fontSize = 11.sp)
        }
        NexaSwitch(
            checked = checked,
            onCheckedChange = onToggle,
            enabled = enabled
        )
    }
}
