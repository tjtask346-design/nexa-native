package com.nexa.app.ui.screens

import android.graphics.BitmapFactory
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
import com.nexa.app.data.CloudinaryHelper
import com.nexa.app.data.KycSubmission
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun KycScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var existing by remember { mutableStateOf<KycSubmission?>(null) }

    fun reload() {
        loading = true
        scope.launch {
            repo.myKyc()
                .onSuccess { r ->
                    existing = r.kyc
                    loading = false
                }
                .onFailure {
                    existing = null
                    loading = false
                }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
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
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Loading…", color = NexaMuted, fontSize = 14.sp)
            }
            existing == null -> KycForm(prefs, repo, scope) { reload() }
            existing?.status == "approved" -> StatusView("approved")
            existing?.status == "rejected" -> StatusView("rejected", existing?.adminNote) {
                existing = null
            }
            else -> StatusView("pending", info = existing?.nidNumber)
        }
    }
}

@Composable
private fun KycForm(
    prefs: Prefs,
    repo: Repository,
    scope: kotlinx.coroutines.CoroutineScope,
    onSubmitted: () -> Unit
) {
    val ctx = LocalContext.current

    var nid by remember { mutableStateOf("") }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var errMsg by remember { mutableStateOf<String?>(null) }

    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) frontUri = it
    }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) backUri = it
    }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) selfieUri = it
    }

    val canSubmit = !busy && nid.length >= 10 &&
        frontUri != null && backUri != null && selfieUri != null

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {

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
                Icon(Icons.Filled.Shield, null, tint = NexaGreen, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Verify your identity",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete KYC to unlock full access\nand faster withdrawals.",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )
        }

        // Info card
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(NexaTeal.copy(alpha = 0.07f))
                .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("✓  Bangladeshi NID card", color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("✓  Passport accepted as alternative", color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("✓  Usually 24–48 hours", color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(24.dp))

        // NID Number
        Text(
            "NID NUMBER",
            color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 10.dp)
        )
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(NexaSurface)
                .border(1.5.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp)
        ) {
            BasicTextField(
                value = nid,
                onValueChange = { nid = it.filter { c -> c.isDigit() }.take(17) },
                singleLine = true,
                textStyle = TextStyle(color = NexaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(NexaGreen),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp)
            ) { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (nid.isEmpty()) {
                        Text("10 or 17 digit NID", color = NexaDim, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    inner()
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "DOCUMENT IMAGES",
            color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 12.dp)
        )

        ImageSlot("Front of NID", "Name, photo, number visible", frontUri, { frontPicker.launch("image/*") }, { frontUri = null })
        Spacer(Modifier.height(12.dp))
        ImageSlot("Back of NID", "Clear photo of back side", backUri, { backPicker.launch("image/*") }, { backUri = null })
        Spacer(Modifier.height(12.dp))
        ImageSlot("Selfie", "Well-lit, no hat/sunglasses", selfieUri, { selfiePicker.launch("image/*") }, { selfieUri = null })

        if (errMsg != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NexaRed.copy(alpha = 0.1f))
                    .border(1.dp, NexaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(errMsg!!, color = NexaRed, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(26.dp))

        GradientButton(
            text = if (busy) statusText.ifEmpty { "Uploading…" } else "Submit for Verification",
            enabled = canSubmit,
            loading = busy,
            onClick = {
                scope.launch {
                    busy = true; errMsg = null
                    try {
                        statusText = "Uploading front…"
                        val frontUrl = CloudinaryHelper.uploadKycImage(ctx, frontUri!!, "front")
                        statusText = "Uploading back…"
                        val backUrl = CloudinaryHelper.uploadKycImage(ctx, backUri!!, "back")
                        statusText = "Uploading selfie…"
                        val selfieUrl = CloudinaryHelper.uploadKycImage(ctx, selfieUri!!, "selfie")
                        statusText = "Submitting…"
                        val res = repo.submitKyc(nid.trim(), frontUrl, backUrl, selfieUrl)
                        busy = false
                        res.onSuccess { r ->
                            if (r.success) onSubmitted()
                            else errMsg = r.message ?: "Submission failed"
                        }.onFailure { errMsg = it.message ?: "Network error" }
                    } catch (e: Exception) {
                        busy = false
                        errMsg = e.message ?: "Upload failed"
                    }
                }
            }
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "Your data is encrypted and used only for verification.",
            color = NexaDim, fontSize = 11.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun ImageSlot(
    label: String,
    subtitle: String,
    uri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val ctx = LocalContext.current

    // Decode once (composable-safe)
    val bmp = remember(uri) {
        if (uri == null) null
        else try {
            val stream = ctx.contentResolver.openInputStream(uri)
            val b = BitmapFactory.decodeStream(stream)
            stream?.close()
            b
        } catch (_: Exception) { null }
    }

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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(58.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (uri != null) NexaGreen.copy(alpha = 0.15f) else NexaSurface2)
                    .border(1.dp, if (uri != null) NexaGreen.copy(alpha = 0.4f) else NexaBorder.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Icon(Icons.Filled.PhotoCamera, null, tint = NexaMuted, modifier = Modifier.size(24.dp))
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

@Composable
private fun StatusView(status: String, note: String? = null, info: String? = null, onRetry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (icon, tint, title, subtitle) = when (status) {
            "approved" -> Quadruple(Icons.Filled.VerifiedUser, NexaGreen, "You're Verified!", "Your identity has been confirmed.")
            "rejected" -> Quadruple(Icons.Filled.Error, NexaRed, "Verification Failed", note ?: "Documents could not be verified.")
            else -> Quadruple(Icons.Filled.HourglassEmpty, NexaTeal, "Under Review", "We'll review your submission within 24–48 hours.")
        }

        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(tint.copy(alpha = 0.15f))
                .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, lineHeight = 19.sp)

        if (info != null) {
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NID", color = NexaMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    Text(info, color = NexaText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (status == "rejected" && onRetry != null) {
            Spacer(Modifier.height(24.dp))
            GradientButton("Try Again", onClick = onRetry)
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

// Destructuring helper
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component4() = d
