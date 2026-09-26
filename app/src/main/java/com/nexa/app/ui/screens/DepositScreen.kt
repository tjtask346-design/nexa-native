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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.NexaIconButton
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
    val canSubmit = !loading && amtValue >= 10.0 && trxId.length >= 4 && sender.length >= 11

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
    ) {
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
                "Deposit",
                color = NexaText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Info card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexaSurface)
                    .border(1.dp, NexaBorder.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text("Send Money to", color = NexaMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("01711-000000", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Then fill in the form below", color = NexaDim, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Amount
            FieldLabel("Amount (USD) — min $10")
            AmountInput(value = amount, onChange = { amount = it })
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 25, 50, 100).forEach { preset ->
                    QuickChip("$$preset", Modifier.weight(1f)) {
                        amount = preset.toDouble().toString()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FieldLabel("bKash TrxID")
            PlainInput(trxId, { trxId = it.uppercase() }, "e.g. 8N7A2B9C1D")

            Spacer(Modifier.height(16.dp))

            FieldLabel("Your bKash number")
            PlainInput(sender, { sender = it }, "01XXXXXXXXX", KeyboardType.Phone)

            Spacer(Modifier.height(20.dp))

            // Note
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(NexaTeal.copy(alpha = 0.07f))
                    .border(1.dp, NexaTeal.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                    .padding(14.dp)
            ) {
                Text(
                    "ℹ️  Admin manually verifies each payment. Your balance updates after verification — usually within minutes.",
                    color = Color(0xFF7DD3C8),
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
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
                            nav.navigate(Routes.SUCCESS + "?kind=deposit&amount=$amtValue&id=${trxId.trim()}") {
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
private fun FieldLabel(text: String) {
    Text(
        text,
        color = NexaMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun AmountInput(value: String, onChange: (String) -> Unit) {
    val focused = remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (focused.value) NexaSurface2 else NexaSurface)
            .border(
                1.5.dp,
                if (focused.value) NexaGreen.copy(alpha = 0.5f) else NexaBorder.copy(alpha = 0.09f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$", color = NexaGreen, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(6.dp))
        BasicTextField(
            value = value,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onChange(it) },
            singleLine = true,
            textStyle = TextStyle(color = NexaText, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold),
            cursorBrush = SolidColor(NexaGreen),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused.value = it.isFocused }
        ) {
            if (value.isEmpty()) {
                Text("0.00", color = Color(0xFF2C4438), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PlainInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val focused = remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (focused.value) NexaSurface2 else NexaSurface)
            .border(
                1.5.dp,
                if (focused.value) NexaGreen.copy(alpha = 0.5f) else NexaBorder.copy(alpha = 0.09f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = NexaText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(NexaGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused.value = it.isFocused }
        ) {
            if (value.isEmpty()) {
                Text(placeholder, color = NexaDim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NexaSurface)
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
