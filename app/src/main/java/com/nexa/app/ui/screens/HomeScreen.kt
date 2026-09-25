package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.data.Transaction
import com.nexa.app.nav.Routes
import com.nexa.app.ui.theme.*

@Composable
fun HomeScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    var balance by remember { mutableStateOf(0.0) }
    var txs by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var hidden by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { me -> me.user?.let { balance = it.balance } }
        repo.balance().onSuccess { if (it.success) balance = it.balance }
        repo.myTransactions().onSuccess { r ->
            txs = r.transactions.ifEmpty { r.data }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Welcome back 👋", color = NexaMuted, fontSize = 12.sp)
                Text(prefs.name ?: "User", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0F2E21), Color(0xFF143D2C), Color(0xFF0A2620))))
                .padding(22.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL BALANCE", color = NexaText.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(
                        if (hidden) "SHOW" else "HIDE",
                        color = NexaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { hidden = !hidden }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (hidden) "••••••" else "$" + String.format("%.2f", balance),
                    color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (hidden) "≈ ৳ ••••••" else "≈ ৳" + String.format("%.2f", balance * 122),
                    color = NexaMuted, fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionBtn("Deposit", Modifier.weight(1f)) { nav.navigate(Routes.DEPOSIT) }
            ActionBtn("Withdraw", Modifier.weight(1f)) { nav.navigate(Routes.WITHDRAW) }
            ActionBtn("My QR", Modifier.weight(1f)) { nav.navigate(Routes.MYQR) }
            ActionBtn("History", Modifier.weight(1f)) { nav.navigate(Routes.HISTORY) }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Recent Activity", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))

        if (txs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = NexaDim)
            }
        } else {
            txs.take(5).forEach { tx ->
                Box(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(NexaSurface)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                (if (tx.type == "deposit") "Deposit" else "Withdraw") + " · " + tx.method,
                                color = NexaText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                            )
                            Text(tx.createdAt ?: "", color = NexaDim, fontSize = 11.sp)
                        }
                        Text(
                            (if (tx.type == "deposit") "+" else "−") + "$" + String.format("%.2f", tx.amount),
                            color = if (tx.type == "deposit") NexaGreen else NexaText,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBtn(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(NexaSurface2)
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = NexaText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
