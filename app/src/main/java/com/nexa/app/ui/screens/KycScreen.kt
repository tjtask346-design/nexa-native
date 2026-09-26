package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*

@Composable
fun KycScreen(nav: NavController, prefs: Prefs, repo: Repository) {
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

        Column(
            Modifier.weight(1f).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(NexaSurface2),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡", fontSize = 52.sp)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "Verify your identity",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp,
                letterSpacing = (-0.4).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Upload your NID (front & back)\nand a selfie to unlock full access.",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            Spacer(Modifier.height(30.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexaTeal.copy(alpha = 0.10f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    "Coming soon in next update",
                    color = NexaTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
