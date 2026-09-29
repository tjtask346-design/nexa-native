package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.NexaConfig
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaIconButton
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

private val WITHDRAW_OPTIONS = listOf(
    Triple("usdt", "USDT", Color(0xFF26A17B)),
    Triple("ltc",  "Litecoin", Color(0xFF345D9D)),
    Triple("nexa", "Nexa User", NexaGreen)
)

@Composable
fun WithdrawScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selected by remember { mutableStateOf("usdt") }
    var amount by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { it.user?.let { u -> balance = u.balance } }
    }

    val amt = amount.toDoubleOrNull() ?: 0.0
    val fee = when (selected) {
        "usdt" -> 1.0
        "ltc" -> 0.05
        else -> 0.0
    }
    val net = (amt - fee).coerceAtLeast(0.0)
    val canSubmit = !loading && amt >= NexaConfig.MIN_WITHDRAW_USD && amt <= balance && dest.isNotBlank()

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
                "Withdraw",
                color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            // Crypto picker
            Text(
                "CHOOSE CRYPTOCURRENCY",
                color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WITHDRAW_OPTIONS.forEach { (key, label, color) ->
                    val isSel = selected == key
                    Box(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) color.copy(alpha = 0.15f) else NexaSurface)
                            .border(
                                1.5.dp,
                                if (isSel) color.copy(alpha = 0.6f) else NexaBorder.copy(alpha = 0.09f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = key; dest = "" }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (isSel) color else NexaMuted, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }

            // Amount
            Text(
                "AMOUNT (USD)",
                color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(NexaSurface)
                    .border(1.5.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$", color = NexaGreen, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                        singleLine = true,
                        textStyle = TextStyle(color = NexaText, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold),
                        cursorBrush = SolidColor(NexaGreen),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    ) { inner ->
                        Box(Modifier.fillMaxWidth()) {
                            if (amount.isEmpty()) {
                                Text("0.00", color = Color(0xFF2C4438), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            inner()
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Available: $${String.format("%,.2f", balance)}",
                color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 50, 100).forEach { preset ->
                    QuickChip("$$preset", Modifier.weight(1f)) { amount = preset.toDouble().toString() }
                }
                QuickChip("MAX", Modifier.weight(1f)) { amount = String.format("%.2f", balance) }
            }

            Spacer(Modifier.height(24.dp))

            // Destination
            Text(
                if (selected == "nexa") "RECIPIENT HANDLE" else "DESTINATION ADDRESS",
                color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(NexaSurface)
                    .border(1.5.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                BasicTextField(
                    value = dest,
                    onValueChange = { dest = it },
                    singleLine = true,
                    textStyle = TextStyle(color = NexaText, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                    cursorBrush = SolidColor(NexaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        if (dest.isEmpty()) {
                            Text(
                                if (selected == "nexa") "@username" else if (selected == "usdt") "0x... (BEP20)" else "ltc1...",
                                color = NexaDim, fontSize = 13.5.sp, fontWeight = FontWeight.Medium
                            )
                        }
                        inner()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Summary
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Column {
                    KV("Amount", "$${String.format("%,.2f", amt)}", false)
                    KV("Network fee", "$${String.format("%.2f", fee)}", false)
                    KV("You'll receive", "$${String.format("%,.2f", net)}", true)
                }
            }

            // Warning
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NexaTeal.copy(alpha = 0.07f))
                    .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Info, null, tint = NexaTeal, modifier = Modifier.size(16.dp))
                Text(
                    if (selected == "nexa")
                        "Nexa-to-Nexa transfer is instant with 0% fee. Recipient receives USD balance."
                    else
                        "Only send to a valid ${if (selected == "usdt") "BEP20 (BSC)" else "LTC"} address. Wrong network may cause permanent loss.",
                    color = Color(0xFF7DD3C8), fontSize = 11.5.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(26.dp))

            GradientButton(
                text = "Request Withdrawal",
                enabled = canSubmit,
                loading = loading,
                onClick = {
                    scope.launch {
                        loading = true
                        if (selected == "nexa") {
                            // Send internal USD balance to another Nexa user
                            val res = repo.sendMoney(dest.trim(), amt, prefs.pin ?: "")
                            loading = false
                            res.onSuccess {
                                Toast.makeText(ctx, "Sent ✓", Toast.LENGTH_SHORT).show()
                                nav.navigate(Routes.SUCCESS + "?kind=send&amount=$amt&id=${dest.trim()}") { popUpTo(Routes.HOME) }
                            }.onFailure {
                                Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val res = repo.withdrawCrypto(dest.trim(), amt)
                            loading = false
                            res.onSuccess { r ->
                                if (r.success) {
                                    Toast.makeText(ctx, "Withdrawal requested ✓", Toast.LENGTH_SHORT).show()
                                    nav.navigate(Routes.SUCCESS + "?kind=withdraw&amount=$amt") { popUpTo(Routes.HOME) }
                                } else {
                                    Toast.makeText(ctx, r.message ?: "Failed", Toast.LENGTH_LONG).show()
                                }
                            }.onFailure {
                                Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun KV(label: String, value: String, total: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = if (total) NexaGreen else NexaText, fontSize = if (total) 15.sp else 13.sp, fontWeight = FontWeight.Bold)
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
