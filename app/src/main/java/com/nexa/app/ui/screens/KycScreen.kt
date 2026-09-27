package com.nexa.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.KycSubmission
import com.nexa.app.data.CloudinaryHelper
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.NexaPlainField
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun KycScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var existing: KycSubmission? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        repo.myKyc().onSuccess { existing = it.kyc }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexaIconButton(onClick = { nav.popBackStack() }) {
                Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "KYC Verification",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        when {
            loading -> LoadingView()
            existing == null -> KycForm(repo, scope, onSubmitted = {
                existing = KycSubmission(status = "pending")
            })
            existing?.status == "approved" -> ApprovedView()
            existing?.status == "rejected" -> RejectedView(existing?.adminNote, onRetry = {
                existing = null
            })
            else -> PendingView(existing)
        }
    }
}

/* ═══════════════════════════════════════════════════
   FORM — user fills and submits
   ═══════════════════════════════════════════════════ */
@Composable
private fun KycForm(repo: Repository, scope: kotlinx.coroutines.CoroutineScope, onSubmitted: () -> Unit) {
    val ctx = LocalContext.current

    var nidNumber by remember { mutableStateOf("") }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri  by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("") }

    // Gallery pickers
    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) frontUri = it
    }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) backUri = it
    }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) selfieUri = it
    }

    val canSubmit = !busy &&
        nidNumber.length >= 10 &&
        frontUri != null && backUri != null && selfieUri != null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        // Hero
        Column(
            Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen.copy(alpha = 0.15f), NexaTeal.copy(alpha = 0.12f))))
                    .border(1.dp, NexaGreen.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = NexaGreen,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Verify your identity",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete KYC to unlock full access,\nhigher limits and faster withdrawals.",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )
        }

        // Info card
        InfoCard()

        Spacer(Modifier.height(24.dp))

        // NID Number
        Text(
            "NID NUMBER",
            color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        NIDInput(value = nidNumber, onChange = { nidNumber = it.filter { c -> c.isDigit() }.take(17) })

        Spacer(Modifier.height(24.dp))

        // Images
        Text(
            "DOCUMENT IMAGES",
            color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ImageSlotCard(
            label = "Front of NID",
            subtitle = "Clear photo showing name, photo and number",
            uri = frontUri,
            onPick = { frontPicker.launch("image/*") },
            onClear = { frontUri = null }
        )
        Spacer(Modifier.height(12.dp))
        ImageSlotCard(
            label = "Back of NID",
            subtitle = "Clear photo showing the back side",
            uri = backUri,
            onPick = { backPicker.launch("image/*") },
            onClear = { backUri = null }
        )
        Spacer(Modifier.height(12.dp))
        ImageSlotCard(
            label = "Selfie",
            subtitle = "Your face, well lit, no sunglasses or hat",
            uri = selfieUri,
            onPick = { selfiePicker.launch("image/*") },
            onClear = { selfieUri = null }
        )

        // Error
        if (errorMsg != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NexaRed.copy(alpha = 0.1f))
                    .border(1.dp, NexaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(errorMsg!!, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(26.dp))

        GradientButton(
            text = if (busy) statusText.ifEmpty { "Uploading…" } else "Submit for Verification",
            enabled = canSubmit,
            loading = busy,
            onClick = {
                scope.launch {
                    busy = true
                    errorMsg = null
                    try {
                        val userId = prefs.email?.replace("@", "_")?.replace(".", "_") ?: "user"

                        statusText = "Uploading front…"
                        val frontUrl = CloudinaryHelper.uploadKycImage(ctx, frontUri!!, "front")

                        statusText = "Uploading back…"
                        val backUrl = CloudinaryHelper.uploadKycImage(ctx, backUri!!, "back")

                        statusText = "Uploading selfie…"
                        val selfieUrl = CloudinaryHelper.uploadKycImage(ctx, selfieUri!!, "selfie")

                        statusText = "Submitting…"
                        val res = repo.submitKyc(nidNumber.trim(), frontUrl, backUrl, selfieUrl)

                        busy = false
                        res.onSuccess { r ->
                            if (r.success) {
                                onSubmitted()
                            } else {
                                errorMsg = r.message ?: "Submission failed"
                            }
                        }.onFailure {
                            errorMsg = it.message ?: "Network error"
                        }
                    } catch (e: Exception) {
                        busy = false
                        errorMsg = e.message ?: "Upload failed. Check internet."
                    }
                }
            }
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "Your data is encrypted and only used for verification.",
            color = NexaDim, fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun InfoCard() {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(NexaTeal.copy(alpha = 0.07f))
            .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            InfoLine("✓", "Bangladeshi NID card accepted")
            InfoLine("✓", "Passport accepted as alternative")
            InfoLine("✓", "Verification usually takes 24–48 hours")
        }
    }
}

@Composable
private fun InfoLine(mark: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(mark, color = NexaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text, color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun NIDInput(value: String, onChange: (String) -> Unit) {
    val focused = remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (focused.value) NexaSurface2 else NexaSurface)
            .border(
                1.5.dp,
                if (focused.value) NexaGreen.copy(alpha = 0.5f) else NexaBorder.copy(alpha = 0.09f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = NexaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(NexaGreen),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged { focused.value = it.isFocused }
                .padding(vertical = 15.dp)
        ) { inner ->
            Box(Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text("10 or 17 digit NID number", color = NexaDim, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                inner()
            }
        }
    }
}

@Composable
private fun ImageSlotCard(
    label: String,
    subtitle: String,
    uri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onPick
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Preview tile
            Box(
                Modifier.size(58.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (uri != null) NexaGreen.copy(alpha = 0.15f) else NexaSurface2)
                    .border(
                        1.dp,
                        if (uri != null) NexaGreen.copy(alpha = 0.4f) else NexaBorder.copy(alpha = 0.12f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uri != null) {
                    val bmp = remember(uri) {
                        try {
                            val stream = LocalContext.current.contentResolver.openInputStream(uri)
                            val b = android.graphics.BitmapFactory.decodeStream(stream)
                            stream?.close()
                            b
                        } catch (_: Exception) { null }
                    }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(Icons.Filled.CheckCircle, null, tint = NexaGreen, modifier = Modifier.size(28.dp))
                    }
                } else {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = NexaMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = NexaDim, fontSize = 11.sp, lineHeight = 15.sp)
            }

            if (uri != null) {
                Box(
                    Modifier.clip(RoundedCornerShape(9.dp))
                        .background(NexaRed.copy(alpha = 0.12f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClear
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Clear", color = NexaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    Modifier.clip(RoundedCornerShape(9.dp))
                        .background(NexaGreen.copy(alpha = 0.13f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Pick", color = NexaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════
   STATUS VIEWS
   ═══════════════════════════════════════════════════ */

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading…", color = NexaMuted, fontSize = 14.sp)
    }
}

@Composable
private fun PendingView(kyc: KycSubmission?) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(NexaTeal.copy(alpha = 0.15f))
                .border(1.dp, NexaTeal.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.HourglassEmpty,
                contentDescription = null,
                tint = NexaTeal,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Under Review",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Your KYC submission is being reviewed.\nThis usually takes 24–48 hours.",
            color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 19.sp
        )

        Spacer(Modifier.height(28.dp))

        if (kyc?.nidNumber != null) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KVRow("Status", "Pending")
                    KVRow("NID Number", kyc.nidNumber)
                    if (kyc.createdAt != null) KVRow("Submitted", kyc.createdAt.take(10))
                }
            }
        }
    }
}

@Composable
private fun ApprovedView() {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(NexaGreen.copy(alpha = 0.15f))
                .border(1.dp, NexaGreen.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = NexaGreen,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "You're Verified!",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Your identity has been verified.\nYou now have full access to all features.",
            color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 19.sp
        )
    }
}

@Composable
private fun RejectedView(note: String?, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(NexaRed.copy(alpha = 0.12f))
                .border(1.dp, NexaRed.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = NexaRed,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Verification Failed",
            color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            note ?: "Your documents could not be verified. Please try again with clearer photos.",
            color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 19.sp
        )
        Spacer(Modifier.height(28.dp))
        GradientButton(text = "Try Again", onClick = onRetry)
    }
}

@Composable
private fun KVRow(k: String, v: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k, color = NexaMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
        Text(v, color = NexaText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    }
}
