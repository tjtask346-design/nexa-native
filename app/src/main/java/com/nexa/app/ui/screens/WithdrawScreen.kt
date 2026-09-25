package com.nexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.Repository
import com.nexa.app.data.WithdrawRequest
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.components.clickableBack
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WithdrawScreen(nav: NavController, repo: Repository) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
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
            Text("Withdraw", color = NexaText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(24.dp))

        Text("Amount (USD)", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        NexaInput(amount, { amount = it }, "0.00", KeyboardType.Decimal)
        Spacer(Modifier.height(16.dp))
        Text("bKash number", color = NexaMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        NexaInput(dest, { dest = it }, "01XXXXXXXXX", KeyboardType.Phone)
        Spacer(Modifier.height(24.dp))

        GradientButton(
            text = if (loading) "Processing…" else "Request Withdrawal",
            enabled = !loading && (amount.toDoubleOrNull() ?: 0.0) > 0 && dest.length >= 11,
            onClick = {
                scope.launch {
                    loading = true
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val res = repo.withdraw(WithdrawRequest("bkash", amt, dest))
                    loading = false
                    res.onSuccess {
                        Toast.makeText(ctx, "Withdrawal requested ✓", Toast.LENGTH_SHORT).show()
                        nav.navigate(Routes.HOME)
                    }.onFailure {
                        Toast.makeText(ctx, it.message ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
