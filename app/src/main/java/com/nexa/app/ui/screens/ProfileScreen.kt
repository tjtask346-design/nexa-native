package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*

@Composable
fun ProfileScreen(nav: NavController, prefs: Prefs) {
    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Box(
            Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
        )
        Spacer(Modifier.height(14.dp))
        Text(prefs.name ?: "User", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(prefs.email ?: "", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(30.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NexaSurface)
                .padding(6.dp)
        ) {
            Column {
                ProfileRow("My QR Code") { nav.navigate(Routes.MYQR) }
                ProfileRow("Fingerprint") { }
                ProfileRow("Support") { }
                ProfileRow("Log Out") {
                    prefs.clear()
                    nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(label, color = NexaText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
