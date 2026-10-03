package com.nexa.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.BiometricHelper
import com.nexa.app.data.CloudinaryHelper
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.components.NexaFabSheet
import com.nexa.app.ui.components.NexaSwitch
import com.nexa.app.ui.components.TransactionPinModal
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity
    val scope = rememberCoroutineScope()

    var showFabSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var biometricOn by remember { mutableStateOf(prefs.biometricEnabled) }
    var pinVerifyMode by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf(prefs.name ?: "User") }
    var avatarUrl by remember { mutableStateOf(prefs.avatarUrl) }
    var uploading by remember { mutableStateOf(false) }

    val hardwareAvailable = remember { BiometricHelper.isAvailable(ctx) }

    fun openSupportEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:forsell395@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Nexa Support Request")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hi Nexa Support,\n\n" +
                        "My registered email: ${prefs.email ?: "—"}\n" +
                        "Account name: $displayName\n" +
                        "Account number: ${prefs.accountNumber ?: "—"}\n\n" +
                        "My issue:\n"
                )
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(ctx, "No email app. Email: forsell395@gmail.com", Toast.LENGTH_LONG).show()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp)
            ) {
                Text(
                    "Profile",
                    color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 30.dp, bottom = 16.dp)
                )

                // HERO
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { if (!uploading) showEditSheet = true }
                    ) {
                        UserAvatarImage(ctx = ctx, url = avatarUrl)
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp).clip(CircleShape)
                                .background(NexaGreen)
                                .border(2.dp, NexaBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = "Change photo",
                                tint = Color(0xFF04140D),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(displayName, color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(prefs.email ?: "", color = NexaMuted, fontSize = 12.5.sp)

                    Spacer(Modifier.height(12.dp))

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(NexaGreen.copy(alpha = 0.12f))
                            .border(1.dp, NexaGreen.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { if (!uploading) showEditSheet = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, null, tint = NexaGreen, modifier = Modifier.size(13.dp))
                            Text(
                                if (uploading) "Uploading…" else "Edit Profile",
                                color = NexaGreen, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // MENU
                Column(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NexaSurface)
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
                                Toast.makeText(ctx, "No fingerprint on this device", Toast.LENGTH_SHORT).show()
                                return@FingerprintRow
                            }
                            if (wantOn) {
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
                                            if (msg.contains("No biometric", true) ||
                                                msg.contains("not available", true) ||
                                                msg.contains("no fingerprint", true)
                                            ) {
                                                pinVerifyMode = "enable"
                                            }
                                        }
                                    )
                                } else pinVerifyMode = "enable"
                            } else {
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
                                        onError = { }
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
                    ProfileRow(Icons.Filled.Shield, NexaGreen, "Two-Factor Auth", "Enabled — Google Authenticator") {
                        Toast.makeText(ctx, "2FA is always on for security", Toast.LENGTH_SHORT).show()
                    }
                    ProfileRow(Icons.Filled.SupportAgent, NexaTeal, "Support", "Chat with us via email") {
                        openSupportEmail()
                    }
                    ProfileRow(Icons.AutoMirrored.Filled.Logout, NexaRed, "Log Out", "Sign out of your account") {
                        prefs.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                }

                Text(
                    "Nexa v1.0.0",
                    color = NexaDim, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                    textAlign = TextAlign.Center
                )
            }

            NexaBottomBar(
                currentRoute = Routes.PROFILE,
                onNavigate = { nav.navigate(it) },
                onFabClick = { showFabSheet = true }
            )
        }

        if (showFabSheet) {
            NexaFabSheet(
                onDismiss = { showFabSheet = false },
                onNavigate = { nav.navigate(it) }
            )
        }

        if (showEditSheet) {
            EditProfileSheet(
                ctx = ctx,
                currentName = displayName,
                currentAvatarUrl = avatarUrl,
                uploading = uploading,
                onDismiss = { if (!uploading) showEditSheet = false },
                onPickImage = { uri ->
                    scope.launch {
                        uploading = true
                        try {
                            val url = CloudinaryHelper.uploadKycImage(ctx, uri, "avatar")
                            val res = repo.updateAvatar(url)
                            if (res.isSuccess) {
                                prefs.avatarUrl = url
                                avatarUrl = url
                                Toast.makeText(ctx, "Photo updated ✓", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, res.exceptionOrNull()?.message ?: "Upload failed", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Upload error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        uploading = false
                    }
                },
                onSaveName = { newName ->
                    prefs.name = newName
                    displayName = newName
                    showEditSheet = false
                    Toast.makeText(ctx, "Name updated ✓", Toast.LENGTH_SHORT).show()
                }
            )
        }

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

// ═══ Avatar loader — supports HTTP URLs and local files ═══
@Composable
private fun UserAvatarImage(ctx: Context, url: String?) {
    if (url.isNullOrBlank()) {
        Image(
            painterResource(R.drawable.nexa_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val bmp = remember(url) { loadBitmapFromUrl(ctx, url) }
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Image(
            painterResource(R.drawable.nexa_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun loadBitmapFromUrl(ctx: Context, url: String): Bitmap? {
    return try {
        if (url.startsWith("http")) {
            val connection = java.net.URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            connection.getInputStream().use { BitmapFactory.decodeStream(it) }
        } else {
            val uri = Uri.parse(url)
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    } catch (e: Exception) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    ctx: Context,
    currentName: String,
    currentAvatarUrl: String?,
    uploading: Boolean,
    onDismiss: () -> Unit,
    onPickImage: (Uri) -> Unit,
    onSaveName: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(currentName) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onPickImage(uri)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NexaSurface,
        dragHandle = null
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            Box(
                Modifier.width(38.dp).height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NexaSurface3)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))
            Text("Edit Profile", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text("Change nickname and profile picture", color = NexaMuted, fontSize = 12.5.sp)

            Spacer(Modifier.height(24.dp))

            // Avatar picker
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(100.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
                        .clickable(
                            enabled = !uploading,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { picker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarImage(ctx = ctx, url = currentAvatarUrl)
                    if (uploading) {
                        Box(
                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Uploading…", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (uploading) "Uploading photo…" else "Tap to change photo",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(26.dp))

            Text(
                "NICKNAME",
                color = NexaMuted, fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface2)
                    .border(1.5.dp, NexaBorder.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = NexaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(NexaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) { inner ->
                    Box {
                        if (name.isEmpty()) {
                            Text("Your display name", color = NexaDim, fontSize = 15.sp)
                        }
                        inner()
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${name.length}/24 characters",
                color = NexaDim, fontSize = 11.sp, fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(28.dp))

            val enabled = name.trim().isNotEmpty()
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (enabled) Brush.horizontalGradient(listOf(NexaGreen, NexaTeal))
                        else Brush.horizontalGradient(listOf(NexaDim, NexaDim))
                    )
                    .clickable(
                        enabled = enabled && !uploading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSaveName(name.trim()) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Save Name",
                    color = Color(0xFF04140D),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface2)
                    .clickable(
                        enabled = !uploading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
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
        NexaSwitch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
    }
}
