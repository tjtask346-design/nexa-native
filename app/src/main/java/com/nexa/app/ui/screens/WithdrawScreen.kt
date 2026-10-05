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

private val NEXA_SUB_OPTIONS = listOf(
    "usdt" to "USDT",
    "ltc"  to "LTC"
)

@Composable
fun WithdrawScreen(nav: NavController, prefs: Prefs, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selected by remember { mutableStateOf("usdt") }
    var nexaSubCurrency by remember { mutableStateOf("usdt") } // for Nexa User tab
    var amount by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var balanceUsd by remember { mutableStateOf(0.0) }
    var balanceLtc by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.me().onSuccess { res ->
            res.user?.let { u ->
                balanceUsd = u.balance
                balanceLtc = u.ltcBalance
            }
        }
    }

    val amt = amount.toDoubleOrNull() ?: 0.0

    // ═══ Effective currency for calculations ═══
    // If "nexa" tab selected, use the sub-currency
    val effectiveCurrency = if (selected == "nexa") nexaSubCurrency else selected

    // ═══ Min + fee based on effective currency + context ═══
    val minWithdraw: Double
    val fee: Double
    val availableBalance: Double

    when (effectiveCurrency) {
        "usdt" -> {
            if (selected == "nexa") {
                // Internal Nexa transfer — very low min, free
                minWithdraw = 0.02
                fee = 0.0
                availableBalance = balanceUsd
            } else {
                // External USDT withdrawal
                minWithdraw = NexaConfig.MIN_WITHDRAW_USDT_USD
                fee = NexaConfig.NETWORK_FEE_USDT_USD
                availableBalance = balanceUsd
            }
        }
        "ltc" -> {
            if (selected == "nexa") {
                // Internal LTC transfer — very low min, free
                minWithdraw = 0.0001
                fee = 0.0
                availableBalance = balanceLtc
            } else {
                // External LTC withdrawal
                minWithdraw = 0.0005
                fee = NexaConfig.NETWORK_FEE_LTC_USD
                availableBalance = balanceLtc
            }
        }
        else -> {
            minWithdraw = NexaConfig.MIN_WITHDRAW_NEXA_USD
            fee = 0.0
            availableBalance = balanceUsd
        }
    }

    val net = (amt - fee).coerceAtLeast(0.0)
    val canSubmit = !loading && amt >= minWithdraw && amt <= availableBalance && dest.isNotBlank()

    // ═══ Labels ═══
    val minLabel = when (effectiveCurrency) {
        "usdt" -> if (selected == "nexa") "Minimum: \$0.02" else "Minimum: 3 USDT (\$3.00)"
        "ltc"  -> if (selected == "nexa") "Minimum: 0.0001 LTC" else "Minimum: 0.0005 LTC"
        else   -> "Minimum: \$20.00"
    }
    val feeLabel = if (fee == 0.0) "Network fee: Free"
                   else "Network fee: \$${String.format("%.2f", fee)}"
    val availableLabel = when (effectiveCurrency) {
        "ltc" -> "Available: ${String.format("%.6f", availableBalance)} LTC"
        else  -> "Available: \$${String.format("%,.2f", availableBalance)}"
    }

    Column(Modifier.fillMaxSize().background(NexaBg)) {
        // Header
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
            // ═══ Main tabs ═══
            Text(
                "CHOOSE METHOD",
                color = NexaMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                Modifier.fillMaxWidth().padding(bottom = if (selected == "nexa") 12.dp else 20.dp),
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
                            ) {
                                selected = key
                                dest = ""
                                amount = ""
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSel) color else NexaMuted,
                            fontWeight = FontWeight.Bold, fontSize = 12.sp
                        )
                    }
                }
            }

            // ═══ Sub-currency selector (only for Nexa User) ═══
            if (selected == "nexa") {
                Text(
                    "SELECT TOKEN",
                    color = NexaMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NEXA_SUB_OPTIONS.forEach { (key, label) ->
                        val isSel = nexaSubCurrency == key
                        val accentColor = if (key == "ltc") Color(0xFF345D9D) else Color(0xFF26A17B)
                        Box(
                            Modifier.weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) accentColor.copy(alpha = 0.15f) else NexaSurface)
                                .border(
                                    1.5.dp,
                                    if (isSel) accentColor.copy(alpha = 0.6f) else NexaBorder.copy(alpha = 0.09f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    nexaSubCurrency = key
                                    amount = ""
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSel) accentColor else NexaMuted,
                                fontWeight = FontWeight.Bold, fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ═══ Info card ═══
            Box(
                Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NexaTeal.copy(alpha = 0.07f))
                    .border(1.dp, NexaTeal.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Info, null, tint = NexaTeal, modifier = Modifier.size(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(minLabel, color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(feeLabel, color = Color(0xFF7DD3C8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(availableLabel, color = NexaDim, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ═══ Amount ═══
            Text(
                if (effectiveCurrency == "ltc") "AMOUNT (LTC)" else "AMOUNT (USD)",
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
                    Text(
                        if (effectiveCurrency == "ltc") "Ł" else "$",
                        color = NexaGreen, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.width(6.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = {
                            val re = if (effectiveCurrency == "ltc")
                                Regex("^\\d*\\.?\\d{0,6}$")
                            else
                                Regex("^\\d*\\.?\\d{0,2}$")
                            if (it.isEmpty() || it.matches(re)) amount = it
                        },
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

            Spacer(Modifier.height(12.dp))

            // ═══ Quick chips ═══
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (effectiveCurrency == "ltc") {
                    val presets = if (selected == "nexa")
                        listOf(0.0005, 0.001, 0.005)
                    else
                        listOf(0.001, 0.005, 0.01)
                    presets.forEach { preset ->
                        QuickChip("${preset}", Modifier.weight(1f)) {
                            amount = preset.toString()
                        }
                    }
                } else {
                    val presets = if (selected == "nexa")
                        listOf(0.10, 1.0, 5.0)
                    else
                        listOf(3.0, 10.0, 25.0)
                    presets.forEach { preset ->
                        val label = if (preset < 1) "\$${String.format("%.2f", preset)}"
                                    else "\$${preset.toInt()}"
                        QuickChip(label, Modifier.weight(1f)) {
                            amount = String.format("%.2f", preset)
                        }
                    }
                }
                QuickChip("MAX", Modifier.weight(1f)) {
                    amount = if (effectiveCurrency == "ltc")
                        String.format("%.6f", availableBalance)
                    else
                        String.format("%.2f", availableBalance)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══ Destination ═══
            Text(
                if (selected == "nexa") "RECIPIENT ACCOUNT NUMBER"
                else "DESTINATION ADDRESS",
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
                                when {
                                    selected == "nexa" -> "10-digit account number"
                                    selected == "usdt" -> "0x... (BEP20 address)"
                                    else               -> "ltc1... (Litecoin address)"
                                },
                                color = NexaDim, fontSize = 13.5.sp, fontWeight = FontWeight.Medium
                            )
                        }
                        inner()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══ Summary ═══
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Column {
                    val amtDisplay = if (effectiveCurrency == "ltc")
                        "${amt} LTC"
                    else
                        "\$${String.format("%,.2f", amt)}"
                    val feeDisplay = if (fee == 0.0) "Free" else "\$${String.format("%.2f", fee)}"
                    val netDisplay = if (effectiveCurrency == "ltc")
                        "${(net * 1.0)} LTC"
                    else
                        "\$${String.format("%,.2f", net)}"

                    KV("Amount", amtDisplay, false)
                    KV(
                        if (selected == "nexa") "Transfer fee" else "Network fee",
                        feeDisplay, false
                    )
                    KV("You'll receive", netDisplay, true)
                }
            }

            // ═══ Warning ═══
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
                    when {
                        selected == "nexa" && effectiveCurrency == "ltc" ->
                            "Nexa-to-Nexa LTC transfer is instant with 0% fee. Recipient receives LTC balance."
                        selected == "nexa" ->
                            "Nexa-to-Nexa USDT transfer is instant with 0% fee. Recipient receives USDT balance."
                        selected == "usdt" ->
                            "Only send to a valid BEP20 (BSC) address. Wrong network may cause permanent loss. Min withdrawal is 3 USDT."
                        else ->
                            "Only send to a valid Litecoin (LTC) address. Wrong network may cause permanent loss. Min withdrawal is 0.0005 LTC."
                    },
                    color = Color(0xFF7DD3C8), fontSize = 11.5.sp,
                    lineHeight = 16.sp, fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(26.dp))

            // ═══ Submit ═══
            GradientButton(
                text = if (amt > 0 && amt < minWithdraw)
                            "Min ${if (effectiveCurrency == "ltc") "$minWithdraw LTC" else "\$${String.format("%.2f", minWithdraw)}"} required"
                        else if (selected == "nexa") "Send to Nexa User"
                        else "Request Withdrawal",
                enabled = canSubmit,
                loading = loading,
                onClick = {
                    scope.launch {
                        loading = true
                        if (selected == "nexa") {
                            // ═══ Internal Nexa transfer with currency ═══
                            val res = repo.sendMoney(dest.trim(), amt, prefs.pin ?: "", effectiveCurrency)
                            loading = false
                            res.onSuccess {
                                Toast.makeText(ctx, "Sent ✓", Toast.LENGTH_SHORT).show()
                                nav.navigate(Routes.SUCCESS + "?kind=send&amount=$amt&id=${dest.trim()}") {
                                    popUpTo(Routes.HOME)
                                }
                            }.onFailure {
                                Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // ═══ External on-chain withdraw ═══
                            val res = repo.withdrawCrypto(dest.trim(), amt)
                            loading = false
                            res.onSuccess { r ->
                                if (r.success) {
                                    Toast.makeText(ctx, "Withdrawal requested ✓", Toast.LENGTH_SHORT).show()
                                    nav.navigate(Routes.SUCCESS + "?kind=withdraw&amount=$amt") {
                                        popUpTo(Routes.HOME)
                                    }
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
