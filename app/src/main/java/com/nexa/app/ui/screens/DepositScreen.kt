package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.nexa.app.data.DepositRequest
import com.nexa.app.data.Repository
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.clickableBack
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NexaInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexaSurface)
            .border(1.dp, NexaGreen.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = NexaDim) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = NexaText,
                unfocusedTextColor = NexaText
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DepositScreen(nav: NavController, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = NexaText, fontSize = 24.sp, modifier = Modifier.clickableBack { nav.popBackStack() })
            Spacer(Modifier.width(16.dp))
            Text("Deposit via bKash", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(24.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NexaSurface)
                .padding(20.dp)
        ) {
            Column {
                Text("Send Money to", color = NexaMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text("01711-000000", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("Then fill the form below and submit", color = NexaDim, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Amount (USD)", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        NexaInput(amount, { amount = it }, "0.00", KeyboardType.Decimal)
        Spacer(Modifier.height(16.dp))
        Text("bKash TrxID", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        NexaInput(trxId, { trxId = it.uppercase() }, "8N7A2B9C1D")
        Spacer(Modifier.height(16.dp))
        Text("Your bKash number", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        NexaInput(sender, { sender = it }, "01XXXXXXXXX", KeyboardType.Phone)
        Spacer(Modifier.height(24.dp))

        GradientButton(
            text = if (loading) "Submitting…" else "Submit for Verification",
            enabled = !loading && (amount.toDoubleOrNull() ?: 0.0) > 0 && trxId.length >= 4 && sender.length >= 11,
            onClick = {
                scope.launch {
                    loading = true
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val res = repo.deposit(DepositRequest("bkash", amt, trxId, sender))
                    loading = false
                    res.onSuccess {
                        Toast.makeText(ctx, "Submitted ✓", Toast.LENGTH_SHORT).show()
                        nav.navigate(Routes.HOME)
                    }.onFailure {
                        Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
