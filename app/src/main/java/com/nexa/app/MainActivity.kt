package com.nexa.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.google.firebase.messaging.FirebaseMessaging
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.NexaNav
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var repo: Repository

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(applicationContext)
        repo = Repository(prefs)

        createNotificationChannel()
        askNotificationPermission()

        setContent {
            NexaTheme {
                Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                    FcmTokenSyncEffect(prefs, repo)
                    OfflineGate {
                        NexaNav(prefs, repo)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "nexa_default",
                "Nexa Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "KYC, deposit, withdrawal alerts" }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

// ═══════════════════════════════════════════════
// FCM TOKEN SYNC
// ═══════════════════════════════════════════════
@Composable
fun FcmTokenSyncEffect(prefs: Prefs, repo: Repository) {

    // Step 1: Fetch FCM token
    LaunchedEffect(Unit) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (!token.isNullOrBlank()) {
                prefs.fcmToken = token
            }
        } catch (_: Exception) { }
    }

    // Step 2: Sync to backend continuously until success
    LaunchedEffect(Unit) {
        var attempts = 0
        while (attempts < 60) {
            try {
                val authToken = prefs.token
                val fcmToken = prefs.fcmToken

                if (!authToken.isNullOrBlank() &&
                    !fcmToken.isNullOrBlank() &&
                    !prefs.fcmTokenSynced
                ) {
                    val res = repo.saveFcmToken(fcmToken)
                    if (res.isSuccess && res.getOrNull()?.success == true) {
                        prefs.fcmTokenSynced = true
                        break
                    }
                }
            } catch (_: Exception) { }

            delay(2000)
            attempts++
        }
    }
}

// ═══════════════════════════════════════════════
// OFFLINE GATE
// ═══════════════════════════════════════════════
@Composable
fun OfflineGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var online by remember { mutableStateOf(isNetworkOnline(context)) }

    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { online = true }
            override fun onLost(network: Network) { online = isNetworkOnline(context) }
            override fun onUnavailable() { online = false }
        }
        try { cm.registerDefaultNetworkCallback(callback) } catch (_: Exception) { }
        onDispose {
            try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) { }
        }
    }

    if (online) content() else OfflineScreen(context)
}

@Composable
private fun OfflineScreen(context: Context) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        Modifier.fillMaxSize().background(NexaBg).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                    .background(NexaRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.WifiOff, null, tint = NexaRed, modifier = Modifier.size(46.dp))
            }
            Text(
                "No Internet Connection",
                color = NexaText, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Nexa needs an active internet connection.\nPlease turn on Wi-Fi or mobile data.",
                color = NexaMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NexaGreen.copy(alpha = 0.15f))
                    .clickable(
                        interactionSource = interaction,
                        indication = null
                    ) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) { }
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    "Open Settings",
                    color = NexaGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                )
            }
        }
    }
}

private fun isNetworkOnline(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (_: Exception) { false }
}
