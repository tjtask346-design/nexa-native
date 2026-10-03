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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScanScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var lastResult by remember { mutableStateOf<String?>(null) }
    var pickedImage by remember { mutableStateOf<Bitmap?>(null) }

    // ═══ Camera QR scanner ═══
    val cameraLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            lastResult = result.contents
            Toast.makeText(ctx, "Scanned: ${result.contents.take(50)}", Toast.LENGTH_LONG).show()
            // TODO: parse and navigate
        }
    }

    // ═══ Gallery picker ═══
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { loadBitmap(ctx, uri) }
                if (bitmap == null) {
                    Toast.makeText(ctx, "Cannot load image", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                pickedImage = bitmap

                // Decode QR from image
                val decoded = withContext(Dispatchers.IO) { decodeQrFromBitmap(bitmap) }
                if (decoded != null) {
                    lastResult = decoded
                    Toast.makeText(ctx, "Scanned: ${decoded.take(50)}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(ctx, "No QR code found in this image", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun launchCamera() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Align QR code inside the frame")
            setBeepEnabled(false)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
        }
        cameraLauncher.launch(options)
    }

    // ═══ Auto-launch camera on first open ═══
    var autoLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoLaunched) {
            autoLaunched = true
            kotlinx.coroutines.delay(300)
            launchCamera()
        }
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(NexaSurface2)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nav.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Scan QR",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Picked image preview or icon
            if (pickedImage != null) {
                Box(
                    Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = pickedImage!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.radialGradient(listOf(NexaGreen.copy(alpha = 0.10f), Color.Transparent)))
                        .border(2.dp, NexaGreen.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = NexaGreen,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Scan a Nexa QR code",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp,
                letterSpacing = (-0.4).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Use camera or pick from gallery",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )

            if (lastResult != null) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NexaSurface)
                        .padding(12.dp)
                ) {
                    Text(
                        "Last scan: $lastResult",
                        color = NexaTeal, fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ═══ Bottom: TWO buttons (Camera + Gallery) ═══
        Row(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Camera button
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(NexaGreen, NexaTeal)))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { launchCamera() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF04140D),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Camera",
                        color = Color(0xFF04140D),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Gallery button
            Box(
                Modifier
                    .weight(1f)
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
                    Icon(
                        Icons.Filled.Collections,
                        contentDescription = null,
                        tint = NexaText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Gallery",
                        color = NexaText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════
private fun loadBitmap(ctx: android.content.Context, uri: Uri): Bitmap? {
    return try {
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) { null }
}

private fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))
        val result = MultiFormatReader().decode(binary)
        result.text
    } catch (_: Exception) { null }
}
