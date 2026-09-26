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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.data.Transaction
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.components.NexaFabSheet
import com.nexa.app.ui.theme.*

@Composable
fun HomeScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    var balance by remember { mutableStateOf(0.0) }
    var txs by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var hidden by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var showFabSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { me ->
            me.user?.let { balance = it.balance }
            loading = false
        }.onFailure { loading = false }
        repo.myTransactions().onSuccess { txs = it.transactions }
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            // Top bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))) {
                    Image(
                        painterResource(R.drawable.nexa_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Welcome back 👋", color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        prefs.name ?: "User",
                        color = NexaText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp
                    )
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(NexaSurface2)
                        .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("🔔", fontSize = 16.sp) }
            }

            // Balance card
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0F2E21), Color(0xFF143D2C), Color(0xFF0A2620))))
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
                                    indication = null
                                ) { hidden = !hidden }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (hidden) "SHOW" else "HIDE",
                                color = NexaGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (hidden) "••••••" else "$" + String.format("%,.2f", balance),
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
                        BalanceChip("This Month In", "+$0.00", Modifier.weight(1f))
                        BalanceChip("This Month Out", "−$0.00", Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick actions
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAction("Deposit", "↓", NexaGreen, Modifier.weight(1f)) { nav.navigate(Routes.DEPOSIT) }
                QuickAction("Withdraw", "↑", NexaTeal, Modifier.weight(1f)) { nav.navigate(Routes.WITHDRAW) }
                QuickAction("My QR", "▢", NexaGreen, Modifier.weight(1f)) { nav.navigate(Routes.MYQR) }
                QuickAction("History", "◷", NexaTeal, Modifier.weight(1f)) { nav.navigate(Routes.HISTORY) }
            }

            Spacer(Modifier.height(24.dp))

            // Recent transactions
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "See all",
                    color = NexaTeal,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nav.navigate(Routes.HISTORY) }
                )
            }
            Spacer(Modifier.height(12.dp))

            if (txs.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Text(if (loading) "Loading…" else "No transactions yet", color = NexaDim, fontSize = 13.sp)
                }
            } else {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    txs.take(5).forEach { tx -> TxRow(tx) }
                }
            }
        }

        NexaBottomBar(
            currentRoute = Routes.HOME,
            onNavigate = { nav.navigate(it) },
            onFabClick = { showFabSheet = true }
        )
    }

    if (showFabSheet) {
        NexaFabSheet(
            onDismiss = { showFabSheet = false },
            onNavigate = { nav.navigate(it) }
        )
    }
}

@Composable
private fun BalanceChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x8004140D))
            .border(1.dp, NexaGreen.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(label, color = NexaText.copy(alpha = 0.55f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Text(value, color = NexaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickAction(label: String, icon: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
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
                .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Text(icon, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        Text(label, color = NexaMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TxRow(tx: Transaction) {
    val isIn = tx.type == "deposit"
    val title = when (tx.type) {
        "deposit" -> "Deposit · bKash"
        "cashout" -> "Withdraw · bKash"
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

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isIn) NexaGreen.copy(alpha = 0.15f) else NexaTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isIn) "↓" else "↑",
                color = if (isIn) NexaGreen else NexaTeal,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
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
