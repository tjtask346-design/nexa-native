package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.R
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.data.Transaction
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.BalanceCard
import com.nexa.app.ui.components.MethodCard
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.components.NexaFabSheet
import com.nexa.app.ui.components.QuickAction
import com.nexa.app.ui.components.SectionHeader
import com.nexa.app.ui.components.TxRow
import com.nexa.app.ui.components.UserAvatar
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    var balance by remember { mutableStateOf(0.0) }
    var txs by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var hidden by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var showFabSheet by remember { mutableStateOf(false) }

    var unreadCount by remember { mutableStateOf(0) }
    var lastSeenCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { me ->
            me.user?.let { balance = it.balance }
            loading = false
        }.onFailure { loading = false }
        repo.myTransactions().onSuccess { txs = it.transactions }
    }

    // Polling for notifications
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val res = repo.getNotifications()
                if (res.isSuccess) {
                    val unread = res.getOrNull()?.unread ?: 0
                    unreadCount = unread
                    if (lastSeenCount > 0 && unread > lastSeenCount) {
                        val latest = res.getOrNull()?.notifications?.firstOrNull()
                        if (latest != null) {
                            Toast.makeText(ctx, "${latest.title}\n${latest.body}", Toast.LENGTH_LONG).show()
                        }
                    }
                    lastSeenCount = unread
                }
            } catch (_: Exception) { }
            delay(15_000)
        }
    }

    val monthIn = remember(txs) {
        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        txs.filter { tx ->
            val d = tx.createdAt?.let { parseYearMonth(it) }
            d != null && d.first == y && d.second == m && tx.type == "deposit"
        }.sumOf { it.amount }
    }

    val monthOut = remember(txs) {
        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        txs.filter { tx ->
            val d = tx.createdAt?.let { parseYearMonth(it) }
            d != null && d.first == y && d.second == m &&
                (tx.type == "cashout" || tx.type == "transfer")
        }.sumOf { it.amount }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp)
        ) {
            // TOP BAR
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(prefs = prefs, size = 42.dp, cornerRadius = 14.dp)

                Column(Modifier.weight(1f)) {
                    Text(
                        "Welcome back 👋",
                        color = NexaMuted, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp
                    )
                    Text(
                        prefs.name ?: "User",
                        color = NexaText, fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp, letterSpacing = (-0.2).sp, maxLines = 1
                    )
                }

                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                        .background(NexaSurface2)
                        .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(13.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { nav.navigate(Routes.NOTIFICATIONS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Notifications, "Notifications",
                        tint = NexaText, modifier = Modifier.size(19.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            Modifier.align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp).clip(RoundedCornerShape(9.dp))
                                .background(NexaRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // BALANCE CARD
            BalanceCard(
                balance = balance,
                hidden = hidden,
                onToggleHide = { hidden = !hidden },
                monthIn = monthIn,
                monthOut = monthOut,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // QUICK ACTIONS
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAction("Deposit", Icons.Filled.ArrowDownward, NexaGreen, Modifier.weight(1f)) {
                    nav.navigate(Routes.DEPOSIT)
                }
                QuickAction("Withdraw", Icons.Filled.ArrowUpward, NexaTeal, Modifier.weight(1f)) {
                    nav.navigate(Routes.WITHDRAW)
                }
                QuickAction("Scan", Icons.Filled.QrCodeScanner, NexaGreen, Modifier.weight(1f)) {
                    nav.navigate(Routes.SCAN)
                }
                QuickAction("My QR", Icons.Filled.QrCode2, NexaTeal, Modifier.weight(1f)) {
                    nav.navigate(Routes.MYQR)
                }
            }

            // ═══════════ QUICK DEPOSIT — direct crypto navigation ═══════════
            Spacer(Modifier.height(24.dp))
            Box(Modifier.padding(horizontal = 20.dp)) { SectionHeader("Quick Deposit") }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MethodCard("USDT", "BEP20 · BSC", R.drawable.usdt, Color(0xFF26A17B)) {
                    nav.navigate("${Routes.DEPOSIT}?crypto=usdt")
                }
                MethodCard("Litecoin", "LTC Network", R.drawable.ltc, Color(0xFF345D9D)) {
                    nav.navigate("${Routes.DEPOSIT}?crypto=ltc")
                }
                MethodCard("Nexa User", "0% fee", R.drawable.nexa_logo, NexaGreen) {
                    nav.navigate("${Routes.DEPOSIT}?crypto=nexa")
                }
            }

            // RECENT TRANSACTIONS
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
                        color = NexaDim, fontSize = 13.sp
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

private fun parseYearMonth(iso: String): Pair<Int, Int>? {
    return try {
        val datePart = iso.substring(0, 7)
        val year = datePart.substring(0, 4).toInt()
        val month = datePart.substring(5, 7).toInt() - 1
        year to month
    } catch (_: Exception) { null }
}
