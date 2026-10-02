package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.nexa.app.data.Prefs
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    var lastResult by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            lastResult = result.contents
            Toast.makeText(ctx, "Scanned: ${result.contents.take(40)}", Toast.LENGTH_LONG).show()
            // TODO: parse nexa://pay?... and navigate to Send screen
        }
        // Always pop back to previous screen (Home), whether scanned or canceled
        nav.popBackStack()
    }

    fun launchScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Align QR code inside the frame")
            setBeepEnabled(false)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
        }
        launcher.launch(options)
    }

    // ⚡ Instant auto-launch (100ms — barely visible)
    LaunchedEffect(Unit) {
        delay(100)
        launchScanner()
    }

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

            Spacer(Modifier.height(28.dp))
            Text(
                "Opening camera…",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp,
                letterSpacing = (-0.4).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Point your camera at any Nexa QR to\nsend money instantly — 0% fee",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )
        }
    }
}
