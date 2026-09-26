package com.nexa.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.NexaConfig
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaAmountField
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.NexaPlainField
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WithdrawScreen(nav: NavController, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { repo.me().onSuccess { it.user?.let { u -> balance = u.balance } } }

    val amtValue = amount.toDoubleOrNull() ?: 0.0
    val canSubmit = !loading && amtValue >= NexaConfig.MIN_WITHDRAW_USD &&
        amtValue <= balance && sender.replace(Regex("[^0-9]"), "").length >= 11

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexaIconButton(onClick = { nav.popBackStack() }) {
                Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("Withdraw", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.width(40.dp))
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Amount (USD) — min \$${NexaConfig.MIN_WITHDRAW_USD.toInt()}",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp))
            NexaAmountField(value = amount, onChange = { amount = it })

            Spacer(Modifier.height(8.dp))
            Text("Available balance: \$${String.format("%,.2f", balance)}",
                color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp))

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 50, 100).forEach { preset ->
                    QuickChip("$$preset", Modifier.weight(1f)) {
                        if (balance >= preset) amount = preset.toDouble().toString()
                    }
                }
                QuickChip("MAX", Modifier.weight(1f)) { amount = String.format("%.2f", balance) }
            }

            Spacer(Modifier.height(20.dp))
            Text("bKash number", color = NexaMuted, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            NexaPlainField(value = sender, onChange = { sender = it }, placeholder = "01XXXXXXXXX", keyboardType = KeyboardType.Phone)

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Column {
                    KVRaw("Amount", "\$${String.format("%,.2f", amtValue)}", false)
                    KVRaw("Fee", "\$0.00", false)
                    KVRaw("You'll Receive", "\$${String.format("%,.2f", amtValue)}", true)
                }
            }

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Request Withdrawal",
                enabled = canSubmit,
                loading = loading,
                onClick = {
                    scope.launch {
                        loading = true
                        val res = repo.cashout(amtValue, sender.trim())
                        loading = false
                        res.onSuccess {
                            Toast.makeText(ctx, "Withdrawal requested ✓", Toast.LENGTH_SHORT).show()
                            nav.navigate("success?kind=withdraw&amount=$amtValue") { popUpTo(Routes.HOME) }
                        }.onFailure {
                            Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun KVRaw(label: String, value: String, total: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = if (total) NexaGreen else NexaText,
            fontSize = if (total) 15.sp else 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
}
