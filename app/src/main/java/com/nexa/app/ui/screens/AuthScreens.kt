package com.nexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nexa.app.data.AuthState
import com.nexa.app.data.Prefs
import com.nexa.app.nav.Routes
import com.nexa.app.ui.components.GradientButton
import com.nexa.app.ui.theme.*

@Composable
private fun NexaTextField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(NexaSurface)
            .border(1.dp, NexaGreen.copy(alpha = 0.09f), RoundedCornerShape(17.dp))
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = NexaDim) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = NexaText,
                unfocusedTextColor = NexaText
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LoginScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf(prefs.email ?: "") }
    Box(Modifier.fillMaxSize().background(NexaBg).padding(26.dp)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(60.dp))
            Text("Welcome back 👋", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Spacer(Modifier.height(8.dp))
            Text("Enter your email to continue.", color = NexaMuted, fontSize = 13.sp)
            Spacer(Modifier.height(32.dp))
            NexaTextField(email, { email = it }, "you@example.com", KeyboardType.Email)
            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = "Continue with Email",
                enabled = email.isNotBlank(),
                onClick = {
                    AuthState.pendingEmail = email.trim()
                    AuthState.pinMode = "login"
                    prefs.email = email.trim()
                    nav.navigate(Routes.PIN)
                }
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = NexaMuted, fontSize = 13.sp)
                Text(
                    "Sign up", color = NexaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.clickable { nav.navigate(Routes.SIGNUP) }
                )
            }
        }
    }
}

@Composable
fun SignupScreen(nav: NavController, prefs: Prefs) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(NexaBg).padding(26.dp)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(50.dp))
            Text("Create your account", color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Spacer(Modifier.height(8.dp))
            Text("Email-only registration — 30 seconds.", color = NexaMuted, fontSize = 13.sp)
            Spacer(Modifier.height(28.dp))
            NexaTextField(email, { email = it }, "you@example.com", KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            NexaTextField(name, { name = it }, "Full name")
            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = "Create Free Account",
                enabled = email.isNotBlank() && name.isNotBlank(),
                onClick = {
                    AuthState.pendingEmail = email.trim()
                    AuthState.pendingName = name.trim()
                    AuthState.pinMode = "setup"
                    prefs.email = email.trim()
                    prefs.name = name.trim()
                    nav.navigate(Routes.PIN)
                }
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = NexaMuted, fontSize = 13.sp)
                Text(
                    "Log in", color = NexaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.clickable { nav.navigate(Routes.LOGIN) }
                )
            }
        }
    }
}
