package com.nexa.app.ui.screens

import androidx.compose.foundation.background
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
import com.nexa.app.ui.components.clickableBack
import com.nexa.app.ui.theme.*

@Composable
fun HistoryScreen(nav: NavController, repo: Repository) {
    var txs by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    LaunchedEffect(Unit) {
        repo.myTransactions().onSuccess { r -> txs = r.transactions.ifEmpty { r.data } }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = NexaText, fontSize = 24.sp, modifier = Modifier.clickableBack { nav.popBackStack() })
            Spacer(Modifier.width(16.dp))
            Text("History", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(24.dp))

        if (txs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = NexaDim)
            }
        }
        txs.forEach { tx ->
            Box(
                Modifier
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
