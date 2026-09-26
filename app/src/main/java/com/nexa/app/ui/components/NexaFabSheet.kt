package com.nexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexaFabSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun go(route: String) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            onNavigate(route)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NexaSurface,
        scrimColor = Color(0xCC000000),
        dragHandle = null
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .padding(bottom = 8.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NexaSurface3)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                "Quick actions",
                color = NexaText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                letterSpacing = (-0.3).sp
            )
            Text(
                "What would you like to do?",
                color = NexaMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            SheetItem(
                "Deposit",
                "Add funds via bKash",
                Icons.Filled.ArrowDownward,
                NexaGreen
            ) { go(Routes.DEPOSIT) }

            SheetItem(
                "Withdraw",
                "Cash out to your bKash",
                Icons.Filled.ArrowUpward,
                NexaTeal
            ) { go(Routes.WITHDRAW) }

            SheetItem(
                "Send Money",
                "Nexa-to-Nexa · instant · 0% fee",
                Icons.Filled.Send,
                NexaGreen
            ) { go(Routes.SEND) }

            SheetItem(
                "My QR Code",
                "Receive instant payments",
                Icons.Filled.QrCode,
                NexaTeal
            ) { go(Routes.MYQR) }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SheetItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(NexaSurface2)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(17.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}
