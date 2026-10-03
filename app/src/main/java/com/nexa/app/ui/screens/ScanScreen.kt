package com.nexa.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScanScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var cameraCancelled by remember { mutableStateOf(false) }

    fun handleScanned(value: String) {
        val recipient = parseRecipient(value)
        if (recipient.isBlank()) {
            Toast.makeText(ctx, "Invalid Nexa QR", Toast.LENGTH_SHORT).show()
            nav.popBackStack()
            return
        }
        nav.navigate("${Routes.SEND}?recipient=$recipient") {
            popUpTo(Routes.HOME)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleScanned(result.contents)
        } else {
            cameraCancelled = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadBitmap(ctx, uri) }
            if (bmp == null) {
                Toast.makeText(ctx, "Cannot load image", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val decoded = withContext(Dispatchers.IO) { decodeQr(bmp) }
            if (decoded != null) handleScanned(decoded)
            else Toast.makeText(ctx, "No QR code in this image", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCamera() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Align Nexa QR inside the frame")
            setBeepEnabled(false)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
        }
        cameraLauncher.launch(options)
    }

    // Immediately launch camera
    LaunchedEffect(Unit) { launchCamera() }

    Box(
        Modifier.fillMaxSize().background(NexaBg),
        contentAlignment = Alignment.Center
    ) {
        if (cameraCancelled) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                        .background(NexaGreen.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        null, tint = NexaGreen,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Text(
                    "Scan a Nexa QR",
                    color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp
                )
                Text(
                    "Camera is closed. Try again or pick from gallery.",
                    color = NexaMuted, fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(NexaGreen, NexaTeal)))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { cameraCancelled = false; launchCamera() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            null, tint = Color(0xFF04140D),
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Open Camera", color = Color(0xFF04140D), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }

                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexaSurface)
                        .border(1.5.dp, NexaBorder.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { galleryLauncher.launch("image/*") }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Collections, null, tint = NexaText, modifier = Modifier.size(18.dp))
                        Text("Pick from Gallery", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }

                Text(
                    "Cancel",
                    color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nav.popBackStack() }
                )
            }
        }
    }
}

private fun parseRecipient(raw: String): String {
    return try {
        val uri = Uri.parse(raw)
        val acc = uri.getQueryParameter("acc") ?: ""
        if (acc.isNotBlank()) return acc
        val to = uri.getQueryParameter("to") ?: ""
        if (to.isNotBlank()) return to.replace("@", "")
        val email = uri.getQueryParameter("email") ?: ""
        if (email.isNotBlank()) return email
        if (raw.all { it.isDigit() }) return raw
        ""
    } catch (e: Exception) { "" }
}

private fun loadBitmap(ctx: android.content.Context, uri: Uri): Bitmap? {
    return try {
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) { null }
}

private fun decodeQr(bitmap: Bitmap): String? {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))
        MultiFormatReader().decode(binary).text
    } catch (_: Exception) { null }
}
