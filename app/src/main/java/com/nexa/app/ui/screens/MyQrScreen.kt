package com.nexa.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nexa.app.data.Prefs
import com.nexa.app.ui.components.clickableBack
import com.nexa.app.ui.theme.*

@Composable
fun MyQrScreen(nav: NavController, prefs: Prefs) {
    val payload = "nexa://pay?email=${prefs.email ?: ""}"
    val bmp = remember { generateQr(payload) }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = NexaText, fontSize = 24.sp, modifier = Modifier.clickableBack { nav.popBackStack() })
            Spacer(Modifier.width(16.dp))
            Text("My QR Code", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(30.dp))

        Text(prefs.name ?: "User", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text("@${(prefs.email ?: "").substringBefore("@")}", color = NexaMuted, fontSize = 13.sp)

        Spacer(Modifier.height(30.dp))

        Box(
            Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            bmp?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Share this QR to receive payments", color = NexaMuted, fontSize = 12.sp)
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
    } catch (e: Exception) {
        null
    }
}
