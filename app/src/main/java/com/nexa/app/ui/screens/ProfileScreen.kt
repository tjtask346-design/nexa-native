package com.nexa.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.theme.*

@Composable
fun ProfileScreen(nav: NavController, prefs: Prefs) {
    Column(Modifier.fillMaxSize().background(NexaBg)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            Text(
                "Profile",
                color = NexaText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp, top = 30.dp, bottom = 16.dp)
            )

            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
                ) {
                    Image(
                        painterResource(R.drawable.nexa_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(prefs.name ?: "User", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(prefs.email ?: "", color = NexaMuted, fontSize = 12.5.sp)
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(NexaTeal.copy(alpha = 0.11f))
                        .border(1.dp, NexaTeal.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text("● ACTIVE", color = NexaTeal, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexaSurface)
            ) {
                ProfileRow("📱", "My QR Code", "Receive instant Nexa payments") { nav.navigate(Routes.MYQR) }
                ProfileRow("👆", "Fingerprint", "Biometric unlock enabled") { }
                ProfileRow("🛡️", "KYC Verification", "Complete your verification") { }
                ProfileRow("💬", "Support", "24/7 live chat") { }
                ProfileRow("🚪", "Log Out", "Sign out of your account") {
                    prefs.clear()
                    nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            }

            Text(
                "Nexa v1.0.0",
                color = NexaDim,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        NexaBottomBar(Routes.PROFILE) { nav.navigate(it) }
    }
}

@Composable
private fun ProfileRow(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(NexaSurface3),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 15.sp) }
        Column(Modifier.weight(1f)) {
            Text(title, color = NexaText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
            Text(subtitle, color = NexaDim, fontSize = 11.sp)
        }
        Text("›", color = NexaDim, fontSize = 18.sp)
    }
}
