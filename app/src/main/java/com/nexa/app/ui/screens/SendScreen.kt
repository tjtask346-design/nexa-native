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
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaAmountField
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.components.NexaPlainField
import com.nexa.app.ui.components.TransactionPinModal
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SendScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }
    var showPinModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { repo.me().onSuccess { it.user?.let { u -> balance = u.balance } } }

    val amtValue = amount.toDoubleOrNull() ?: 0.0
    val canSubmit = !loading && recipient.isNotBlank() && amtValue >= 1.0 && amtValue <= balance

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexaIconButton(onClick = { nav.popBackStack() }) {
                Text("←", color = NexaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Send Money",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Text(
                "Recipient (email or account number)",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaPlainField(
                value = recipient,
                onChange = { recipient = it },
                placeholder = "email or account number"
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Amount (USD)",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaAmountField(value = amount, onChange = { amount = it })

            Spacer(Modifier.height(8.dp))
            Text(
                "Available: \$${String.format("%,.2f", balance)}",
                color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 25, 50).forEach { preset ->
                    QuickChip("$$preset", Modifier.weight(1f)) {
                        if (balance >= preset) amount = preset.toDouble().toString()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Note (optional)",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaPlainField(
                value = note,
                onChange = { note = it },
                placeholder = "What's this for?"
            )

            Spacer(Modifier.height(20.dp))

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Column {
                    KVRaw("Amount", "\$${String.format("%,.2f", amtValue)}", false)
                    KVRaw("Network fee", "\$0.00", false)
                    KVRaw("Total", "\$${String.format("%,.2f", amtValue)}", true)
                }
            }

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Send Now",
                enabled = canSubmit,
                loading = loading,
                onClick = { showPinModal = true }
            )
            Spacer(Modifier.height(30.dp))
        }
    }

    TransactionPinModal(
        visible = showPinModal,
        title = "Send \$${String.format("%,.2f", amtValue)}",
        subtitle = "To: $recipient",
        amount = "\$${String.format("%,.2f", amtValue)}",
        prefs = prefs,
        expectedPin = prefs.pin,
        onDismiss = { showPinModal = false },
        onConfirmed = {
            showPinModal = false
            scope.launch {
                loading = true
                val pin = prefs.pin ?: ""
                val res = repo.sendMoney(recipient.trim(), amtValue, pin)
                loading = false
                res.onSuccess {
                    Toast.makeText(ctx, "Sent ✓", Toast.LENGTH_SHORT).show()
                    nav.navigate("success?kind=send&amount=$amtValue&id=$recipient") {
                        popUpTo(Routes.HOME)
                    }
                }.onFailure {
                    Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
}

@Composable
private fun KVRaw(label: String, value: String, total: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(
            value,
            color = if (total) NexaGreen else NexaText,
            fontSize = if (total) 15.sp else 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
