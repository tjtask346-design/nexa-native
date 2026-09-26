package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*

@Composable
fun NexaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color(0xE006100C))
            .border(0.5.dp, NexaBorder.copy(alpha = 0.09f))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem("Home", Icons.Filled.Home, currentRoute == Routes.HOME, Modifier.weight(1f)) { onNavigate(Routes.HOME) }
        TabItem("History", Icons.Filled.ReceiptLong, currentRoute == Routes.HISTORY, Modifier.weight(1f)) { onNavigate(Routes.HISTORY) }

        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(NexaGreen, NexaGreenDark, NexaTeal)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onFabClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color(0xFF04140D), fontSize = 28.sp, fontWeight = FontWeight.Light)
        }

        TabItem("Scan", Icons.Filled.QrCodeScanner, currentRoute == Routes.SCAN, Modifier.weight(1f)) { onNavigate(Routes.SCAN) }
        TabItem("Profile", Icons.Filled.Person, currentRoute == Routes.PROFILE, Modifier.weight(1f)) { onNavigate(Routes.PROFILE) }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val tint = if (active) NexaGreen else NexaDim
    Column(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}
