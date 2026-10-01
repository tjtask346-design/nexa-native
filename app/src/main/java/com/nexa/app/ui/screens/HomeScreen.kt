package com.nexa.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nexa.app.ui.components.*
import com.nexa.app.ui.theme.*
import java.util.Calendar

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

    val monthIn = remember(txs) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        txs.filter { tx ->
            val d = tx.createdAt?.let { parseYearMonth(it) }
            d != null && d.first == currentYear && d.second == currentMonth && tx.type == "deposit"
        }.sumOf { it.amount }
    }

    val monthOut = remember(txs) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        txs.filter { tx ->
            val d = tx.createdAt?.let { parseYearMonth(it) }
            d != null && d.first == currentYear && d.second == currentMonth &&
            (tx.type == "cashout" || tx.type == "transfer")
        }.sumOf { it.amount }
    }

    NexaScreen {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                // ══════════ TOP BAR ══════════
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painterResource(R.drawable.nexa_logo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Greeting
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Welcome back 👋",
                            color = NexaMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )
                        Text(
                            prefs.name ?: "User",
                            color = NexaText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1
                        )
                    }
                    // Bell
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(NexaSurface2)
                            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔔", fontSize = 16.sp)
                    }
                }

                // ══════════ BALANCE CARD ══════════
                BalanceCard(
                    balance = balance,
                    hidden = hidden,
                    onToggleHide = { hidden = !hidden },
                    monthIn = monthIn,
                    monthOut = monthOut,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // ══════════ QUICK ACTIONS ══════════
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAction("Deposit", "↓", NexaGreen, Modifier.weight(1f)) {
                        nav.navigate(Routes.DEPOSIT)
                    }
                    QuickAction("Withdraw", "↑", NexaTeal, Modifier.weight(1f)) {
                        nav.navigate(Routes.WITHDRAW)
                    }
                    QuickAction("Scan", "▢", NexaGreen, Modifier.weight(1f)) {
                        nav.navigate(Routes.SCAN)
                    }
                    QuickAction("My QR", "◫", NexaTeal, Modifier.weight(1f)) {
                        nav.navigate(Routes.MYQR)
                    }
                }

                // ══════════ QUICK DEPOSIT METHODS ══════════
                Spacer(Modifier.height(24.dp))
                Box(Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader("Quick Deposit")
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MethodCard("bKash", "0% fee", "b", Color(0xFFE2136E)) {
                        nav.navigate(Routes.DEPOSIT)
                    }
                    MethodCard("USDT", "Fee $1", "₮", Color(0xFF26A17B)) {
                        nav.navigate(Routes.DEPOSIT)
                    }
                    MethodCard("Litecoin", "Fee $0.10", "Ł", Color(0xFF345D9D)) {
                        nav.navigate(Routes.DEPOSIT)
                    }
                    MethodCard("Nexa User", "0% fee", "N", NexaGreen) {
                        nav.navigate(Routes.MYQR)
                    }
                }

                // ══════════ RECENT TRANSACTIONS ══════════
                Spacer(Modifier.height(24.dp))
                Box(Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(
                        title = "Recent Transactions",
                        actionText = "See all",
                        onAction = { nav.navigate(Routes.HISTORY) }
                    )
                }
                Spacer(Modifier.height(12.dp))

                if (txs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (loading) "Loading…" else "No transactions yet",
                            color = NexaDim,
                            fontSize = 13.sp
                        )
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

            // ══════════ BOTTOM BAR ══════════
            NexaBottomBar(
                currentRoute = Routes.HOME,
                onNavigate = { nav.navigate(it) },
                onFabClick = { showFabSheet = true }
            )
        }
    }

    if (showFabSheet) {
        NexaFabSheet(
            onDismiss = { showFabSheet = false },
            onNavigate = { nav.navigate(it) }
        )
    }
}

// Helper: parse createdAt ISO string to (year, month)
private fun parseYearMonth(iso: String): Pair<Int, Int>? {
    return try {
        val datePart = iso.substring(0, 7) // "2026-10"
        val year = datePart.substring(0, 4).toInt()
        val month = datePart.substring(5, 7).toInt() - 1
        year to month
    } catch (_: Exception) { null }
}
