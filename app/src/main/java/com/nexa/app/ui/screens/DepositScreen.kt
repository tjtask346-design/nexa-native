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
fun DepositScreen(nav: NavController, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val amtValue = amount.toDoubleOrNull() ?: 0.0
    val canSubmit = !loading &&
        amtValue >= NexaConfig.MIN_DEPOSIT_USD &&
        trxId.length >= 4 &&
        sender.replace(Regex("[^0-9]"), "").length >= 11

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
                "Deposit", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text("Send Money to", color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(NexaConfig.ADMIN_BKASH_NUMBER, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Then fill the form below", color = NexaDim, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Amount (USD) — min \$${NexaConfig.MIN_DEPOSIT_USD.toInt()}",
                color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 10.dp))
            NexaAmountField(value = amount, onChange = { amount = it })

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 25, 50, 100).forEach { preset ->
                    QuickChip("$$preset", Modifier.weight(1f)) {
                        amount = preset.toDouble().toString()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("bKash TrxID", color = NexaMuted, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            NexaPlainField(value = trxId, onChange = { trxId = it.uppercase() }, placeholder = "e.g. 8N7A2B9C1D")

            Spacer(Modifier.height(16.dp))
            Text("Your bKash number", color = NexaMuted, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            NexaPlainField(value = sender, onChange = { sender = it }, placeholder = "01XXXXXXXXX", keyboardType = KeyboardType.Phone)

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(NexaTeal.copy(alpha = 0.07f))
                    .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                    .padding(14.dp)
            ) {
                Text("ℹ️  Admin manually verifies each payment. Your balance updates after verification.",
                    color = Color(0xFF7DD3C8), fontSize = 11.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Submit for Verification",
                enabled = canSubmit,
                loading = loading,
                onClick = {
                    scope.launch {
                        loading = true
                        val res = repo.deposit(amtValue, trxId.trim(), sender.trim())
                        loading = false
                        res.onSuccess {
                            Toast.makeText(ctx, "Submitted ✓", Toast.LENGTH_SHORT).show()
                            nav.navigate("success?kind=deposit&amount=$amtValue&id=${trxId.trim()}") {
                                popUpTo(Routes.HOME)
                            }
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
private fun QuickChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(NexaSurface)
            .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = NexaText, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
}
