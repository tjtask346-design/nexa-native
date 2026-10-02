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
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.NexaNav
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var repo: Repository

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("NEXA_FCM", "Firebase App initialized")
            }
        } catch (e: Exception) {
            Log.e("NEXA_FCM", "Firebase init FAILED", e)
        }

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
// FCM TOKEN SYNC — Background thread to avoid blocking
// ═══════════════════════════════════════════════
@Composable
fun FcmTokenSyncEffect(prefs: Prefs, repo: Repository) {
    val context = LocalContext.current

    // ─── Fetch FCM token on Background thread ───
    LaunchedEffect(Unit) {
        if (!prefs.fcmToken.isNullOrBlank()) {
            Log.d("NEXA_FCM", "Using cached token")
            return@LaunchedEffect
        }

        try {
            Log.d("NEXA_FCM", "Requesting FCM token on IO dispatcher...")
            val token = withContext(Dispatchers.IO) {
                FirebaseMessaging.getInstance().token.await()
            }
            if (!token.isNullOrBlank()) {
                prefs.fcmToken = token
                prefs.fcmTokenSynced = false
                Log.d("NEXA_FCM", "SUCCESS: ${token.take(30)}...")
                Toast.makeText(context, "FCM token OK ✓", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("NEXA_FCM", "Token blank")
                Toast.makeText(context, "FCM token empty", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            val messages = mutableListOf<String>()
            var current: Throwable? = e
            while (current != null) {
                messages.add("${current.javaClass.simpleName}: ${current.message}")
                current = current.cause
            }
            val fullMsg = messages.joinToString(" → ")
            Log.e("NEXA_FCM", "FULL CHAIN: $fullMsg", e)
            Toast.makeText(context, "FCM ERR: $fullMsg", Toast.LENGTH_LONG).show()
        }
    }

    // ─── Sync to backend ───
    LaunchedEffect(Unit) {
        var attempt = 0
        while (attempt < 90) {
            try {
                val auth = prefs.token
                val fcm = prefs.fcmToken
                val synced = prefs.fcmTokenSynced

                if (!auth.isNullOrBlank() && !fcm.isNullOrBlank() && !synced) {
                    val res = repo.saveFcmToken(fcm)
                    if (res.isSuccess && res.getOrNull()?.success == true) {
                        prefs.fcmTokenSynced = true
                        Log.d("NEXA_FCM", "Synced ✓")
                        Toast.makeText(context, "FCM synced ✓", Toast.LENGTH_SHORT).show()
                        return@LaunchedEffect
                    } else {
                        val err = res.exceptionOrNull()?.message ?: "unknown"
                        Log.e("NEXA_FCM", "Sync error: $err")
                        if (attempt == 3) {
                            Toast.makeText(context, "Sync: $err", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (_: Exception) { }

            delay(2000)
            attempt++
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
