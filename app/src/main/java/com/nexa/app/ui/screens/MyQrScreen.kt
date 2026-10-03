package com.nexa.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nexa.app.data.Prefs
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.UserAvatar
import com.nexa.app.ui.theme.*

@Composable
fun MyQrScreen(nav: NavController, prefs: Prefs) {
    val ctx = LocalContext.current
    val handle = "@" + (prefs.email ?: "user").substringBefore("@")
    val payload = "nexa://pay?to=$handle&email=${prefs.email ?: ""}"
    val bmp = remember { generateQr(payload) }

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
                "My QR Code",
                color = NexaText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaGreen.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ═══ User avatar (was initial letter) ═══
                    UserAvatar(
                        prefs = prefs,
                        size = 52.dp,
                        cornerRadius = 17.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        prefs.name ?: "User",
                        color = NexaText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(handle, color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(22.dp))

            // QR code
            Box(
                Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                bmp?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Scan to pay me instantly",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
            Text("0% fee · Instant credit · Nexa-to-Nexa", color = NexaMuted, fontSize = 11.5.sp)

            Spacer(Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton("Share QR", "↑", Modifier.weight(1f)) {
                    val send = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Pay me on Nexa: $handle")
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share via"))
                }
                ActionButton("Copy Link", "⧉", Modifier.weight(1f)) {
                    val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                    cm.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "nexa",
                            "https://nexa.app/${handle.replace("@", "")}"
                        )
                    )
                    android.widget.Toast.makeText(ctx, "Copied ✓", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, color = NexaText, fontSize = 14.sp)
            Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

private fun generateQr(content: String): Bitmap? {
    return try {
        val size = 512
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) for (y in 0 until size) {
            bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
        bmp
    } catch (e: Exception) { null }
}
