package com.nexa.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.app.R
import com.nexa.app.data.Transaction
import com.nexa.app.ui.theme.*

// ═══════════════════════════════════════
// BALANCE CARD
// ═══════════════════════════════════════
@Composable
fun BalanceCard(
    balance: Double,
    hidden: Boolean,
    onToggleHide: () -> Unit,
    monthIn: Double,
    monthOut: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0F2E21), Color(0xFF143D2C), Color(0xFF0A2620))
                )
            )
            .border(1.dp, NexaGreen.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(NexaGreen.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 1.05f, -size.height * 0.1f),
                        radius = size.width * 0.75f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(NexaTeal.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(-size.width * 0.1f, size.height * 1.1f),
                        radius = size.width * 0.75f
                    )
                )
            }
            .padding(22.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL BALANCE",
                    color = NexaText.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, NexaGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleHide
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (hidden) "SHOW" else "HIDE",
                        color = NexaGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                if (hidden) "•••••••" else "$" + String.format("%,.2f", balance),
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.6).sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFA7F3D0)))
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                if (hidden) "≈ ৳ ••••••" else "≈ ৳" + String.format("%,.2f", balance * 122),
                color = NexaText.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MonthChip("This Month In", "+$" + String.format("%,.2f", monthIn), Modifier.weight(1f))
                MonthChip("This Month Out", "−$" + String.format("%,.2f", monthOut), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonthChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x8004140D))
            .border(1.dp, NexaGreen.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = NexaText.copy(alpha = 0.55f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            color = NexaGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
    }
}

// ═══════════════════════════════════════
// QUICK ACTION — With glow + Material Icon
// ═══════════════════════════════════════
@Composable
fun QuickAction(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(NexaSurface2)
                .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(iconColor.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width * 0.75f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            label,
            color = NexaMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        )
    }
}

// ═══════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = NexaText,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = (-0.2).sp
        )
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                color = NexaTeal,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAction
                )
            )
        }
    }
}

// ═══════════════════════════════════════
// QUICK DEPOSIT METHOD CARD — With real image + glow
// ═══════════════════════════════════════
@Composable
fun MethodCard(
    name: String,
    feeText: String,
    imageRes: Int,
    markColor: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(markColor.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.9f
                    )
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(13.dp)
    ) {
        Column {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(markColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = name,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(name, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(2.dp))
            Text(feeText, color = NexaDim, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ═══════════════════════════════════════
// TRANSACTION ROW — With Material Icons + glow
// ═══════════════════════════════════════
@Composable
fun TxRow(tx: Transaction) {
    val isIn = tx.type == "deposit"
    val title = when (tx.type) {
        "deposit" -> "Deposit"
        "cashout" -> "Withdraw"
        "transfer" -> "Sent · Nexa"
        else -> tx.type.replaceFirstChar { it.uppercase() }
    }
    val sign = if (isIn) "+" else "−"
    val pill = when (tx.status) {
        "approved" -> "Completed"
        "pending" -> "Pending"
        else -> "Failed"
    }
    val pillColor = when (tx.status) {
        "approved" -> NexaGreen
        "pending" -> NexaTeal
        else -> NexaRed
    }
    val iconColor = if (isIn) NexaGreen else NexaTeal

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(17.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(iconColor.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(0f, size.height / 2),
                        radius = size.height * 0.9f
                    )
                )
            }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIn) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = NexaText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
            Text(
                (tx.createdAt ?: "").take(10) + " · " + (tx.trxId?.takeLast(6) ?: "—"),
                color = NexaDim,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$sign$${String.format("%,.2f", tx.amount)}",
                color = if (isIn) NexaGreen else NexaText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Box(
                Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(pillColor.copy(alpha = 0.14f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(pill, color = pillColor, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
