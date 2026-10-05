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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private val TOKEN_OPTIONS = listOf(
    Triple("usdt", "USDT", Color(0xFF26A17B)),
    Triple("ltc",  "LTC",  Color(0xFF345D9D))
)

@Composable
fun SendScreen(
    nav: NavController,
    prefs: Prefs,
    repo: Repository,
    prefilledRecipient: String = ""
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var recipient by remember { mutableStateOf(prefilledRecipient) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("usdt") }
    var balanceUsd by remember { mutableStateOf(0.0) }
    var balanceLtc by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }
    var showPinModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { res ->
            res.user?.let { u ->
                balanceUsd = u.balance
                balanceLtc = u.ltcBalance
            }
        }
    }

    val amtValue = amount.toDoubleOrNull() ?: 0.0
    val selectedBalance = if (selectedCurrency == "ltc") balanceLtc else balanceUsd
    val minAmount = if (selectedCurrency == "ltc") 0.0005 else 1.0

    val canSubmit = !loading &&
        recipient.isNotBlank() &&
        amtValue >= minAmount &&
        amtValue <= selectedBalance

    // Display balance based on currency
    val balanceLabel = if (selectedCurrency == "ltc")
        "${String.format("%.6f", balanceLtc)} LTC"
    else
        "$${String.format("%,.2f", balanceUsd)}"

    val amountLabel = if (selectedCurrency == "ltc")
        "${amtValue} LTC"
    else
        "$${String.format("%,.2f", amtValue)}"

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
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            // ═══ Recipient ═══
            Text(
                "RECIPIENT ACCOUNT NUMBER",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaPlainField(
                value = recipient,
                onChange = { recipient = it },
                placeholder = "10-digit account number"
            )

            Spacer(Modifier.height(20.dp))

            // ═══ Token selector ═══
            Text(
                "SELECT TOKEN",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TOKEN_OPTIONS.forEach { (key, label, color) ->
                    val isSel = selectedCurrency == key
                    Box(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) color.copy(alpha = 0.15f) else NexaSurface)
                            .border(
                                1.5.dp,
                                if (isSel) color.copy(alpha = 0.6f) else NexaBorder.copy(alpha = 0.09f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedCurrency = key
                                amount = ""
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSel) color else NexaMuted,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ═══ Amount ═══
            Text(
                if (selectedCurrency == "ltc") "AMOUNT (LTC)" else "AMOUNT (USD)",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaAmountField(
                value = amount,
                onChange = { amount = it }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Available: $balanceLabel",
                color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(10.dp))

            // ═══ Quick chips ═══
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedCurrency == "ltc") {
                    listOf(0.001, 0.005, 0.01).forEach { preset ->
                        QuickChip("${preset} LTC", Modifier.weight(1f)) {
                            if (balanceLtc >= preset) amount = preset.toString()
                        }
                    }
                } else {
                    listOf(5, 10, 25).forEach { preset ->
                        QuickChip("$$preset", Modifier.weight(1f)) {
                            if (balanceUsd >= preset) amount = preset.toDouble().toString()
                        }
                    }
                }
                QuickChip("MAX", Modifier.weight(1f)) {
                    amount = if (selectedCurrency == "ltc")
                        String.format("%.6f", balanceLtc)
                    else
                        String.format("%.2f", balanceUsd)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ Note (optional) ═══
            Text(
                "NOTE (OPTIONAL)",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp)
            )
            NexaPlainField(value = note, onChange = { note = it }, placeholder = "What's this for?")

            Spacer(Modifier.height(20.dp))

            // ═══ Summary card ═══
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Column {
                    KV("Token", selectedCurrency.uppercase())
                    KV("Amount", amountLabel)
                    KV("Network fee", "Free (Nexa internal)")
                    KV("You send", amountLabel, total = true)
                }
            }

            // ═══ Info ═══
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NexaTeal.copy(alpha = 0.07f))
                    .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    if (selectedCurrency == "ltc")
                        "LTC transfer is instant and free between Nexa users. Recipient receives LTC balance."
                    else
                        "USDT transfer is instant and free between Nexa users. Recipient receives USDT balance.",
                    color = Color(0xFF7DD3C8), fontSize = 11.5.sp,
                    lineHeight = 16.sp, fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(24.dp))

            GradientButton(
                text = if (amtValue > 0 && amtValue < minAmount)
                    "Min ${if (selectedCurrency == "ltc") "0.0005 LTC" else "$1"} required"
                else "Send Now",
                enabled = canSubmit,
                loading = loading,
                onClick = { showPinModal = true }
            )
            Spacer(Modifier.height(30.dp))
        }
    }

    TransactionPinModal(
        visible = showPinModal,
        title = "Send $amountLabel",
        subtitle = "To: $recipient",
        amount = amountLabel,
        prefs = prefs,
        expectedPin = prefs.pin,
        onDismiss = { showPinModal = false },
        onConfirmed = {
            showPinModal = false
            scope.launch {
                loading = true
                val pin = prefs.pin ?: ""
                val res = repo.sendMoney(recipient.trim(), amtValue, pin, selectedCurrency)
                loading = false
                res.onSuccess {
                    Toast.makeText(ctx, "Sent ✓", Toast.LENGTH_SHORT).show()
                    nav.navigate("${Routes.SUCCESS}?kind=send&amount=$amtValue&id=$recipient") {
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
private fun KV(label: String, value: String, total: Boolean = false) {
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
    ) { Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
}
