package com.nexa.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*

@Composable
fun ScanScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var handled by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ═══ Scanned → Send ═══
    fun handleResult(text: String) {
        if (handled) return
        handled = true
        val recipient = parseRecipient(text)
        if (recipient.isBlank()) {
            Toast.makeText(ctx, "Invalid Nexa QR", Toast.LENGTH_SHORT).show()
            nav.popBackStack()
            return
        }
        nav.navigate("${Routes.SEND}?recipient=$recipient") {
            popUpTo(Routes.HOME)
        }
    }

    // ═══ Gallery picker ═══
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bmp = try {
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }
        if (bmp == null) {
            Toast.makeText(ctx, "Cannot load image", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        val decoded = decodeQrFromBitmap(bmp)
        if (decoded != null) handleResult(decoded)
        else Toast.makeText(ctx, "No QR code in this image", Toast.LENGTH_LONG).show()
    }

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize().background(NexaBg), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Camera permission required",
                    color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
                Text(
                    "Tap below to grant permission",
                    color = NexaMuted, fontSize = 13.sp
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(NexaGreen.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Grant Permission",
                        color = NexaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ═══════════════════════════════════════════
        // Camera preview via ZXing's DecoratedBarcodeView
        // ═══════════════════════════════════════════
        val barcodeViewRef = remember { mutableStateOf<DecoratedBarcodeView?>(null) }

        AndroidView(
            factory = { context ->
                DecoratedBarcodeView(context).apply {
                    setStatusText("Align Nexa QR inside the frame")
                    decodeContinuous(object : BarcodeCallback {
                        override fun barcodeResult(result: BarcodeResult?) {
                            result?.text?.let { handleResult(it) }
                        }
                        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                            // ignore
                        }
                    })
                }.also { barcodeViewRef.value = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ═══ Camera lifecycle — resume/pause ═══
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                barcodeViewRef.value?.let { view ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> view.resume()
                        Lifecycle.Event.ON_PAUSE -> view.pause()
                        else -> {}
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                barcodeViewRef.value?.pause()
            }
        }

        // ═══════════════════════════════════════════
        // CLOSE button — top-left
        // ═══════════════════════════════════════════
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xCC000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { nav.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // ═══════════════════════════════════════════
        // GALLERY button — bottom center (visible WITH camera)
        // ═══════════════════════════════════════════
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xCC000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { galleryLauncher.launch("image/*") }
                .padding(horizontal = 28.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.Collections,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Pick from Gallery",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════
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

private fun decodeQrFromBitmap(bitmap: android.graphics.Bitmap): String? {
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
