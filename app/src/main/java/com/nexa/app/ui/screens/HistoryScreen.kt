package com.nexa.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Repository
import com.nexa.app.data.Transaction
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.NexaBottomBar
import com.nexa.app.ui.components.NexaFabSheet
import com.nexa.app.ui.theme.*

@Composable
fun HistoryScreen(nav: NavController, repo: Repository) {
    var all by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var filter by remember { mutableStateOf("all") }
    var showFabSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.myTransactions().onSuccess { all = it.transactions }
    }

    val list = when (filter) {
        "deposit" -> all.filter { it.type == "deposit" }
        "cashout" -> all.filter { it.type == "cashout" }
        "transfer" -> all.filter { it.type == "transfer" }
        else -> all
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            Text(
                "Transaction History",
                color = NexaText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp, top = 30.dp, bottom = 16.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip("All", filter == "all") { filter = "all" }
                FilterChip("Deposits", filter == "deposit") { filter = "deposit" }
                FilterChip("Withdrawals", filter == "cashout") { filter = "cashout" }
                FilterChip("Sent", filter == "transfer") { filter = "transfer" }
            }

            if (list.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found", color = NexaDim, fontSize = 13.sp)
                }
            } else {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    list.forEach { tx -> TxRow(tx) }
                }
            }
        }

        NexaBottomBar(
            currentRoute = Routes.HISTORY,
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
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) NexaGreen.copy(alpha = 0.12f) else NexaSurface)
            .border(
                1.dp,
                if (active) NexaGreen.copy(alpha = 0.45f) else NexaBorder.copy(alpha = 0.09f),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = if (active) NexaGreen else NexaMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
