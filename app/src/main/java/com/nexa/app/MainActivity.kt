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
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var repo: Repository

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure Firebase is initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("NEXA_FCM", "Firebase init failed", e)
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
// FCM TOKEN SYNC — with on-screen toasts for debugging
// ═══════════════════════════════════════════════
@Composable
fun FcmTokenSyncEffect(prefs: Prefs, repo: Repository) {
    val context = LocalContext.current

    // ─── Step 1: Fetch FCM token (once) ───
    LaunchedEffect(Unit) {
        if (prefs.fcmToken.isNullOrBlank()) {
            try {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            if (!token.isNullOrBlank()) {
                                prefs.fcmToken = token
                                Log.d("NEXA_FCM", "Token: ${token.take(20)}...")
                                Toast.makeText(
                                    context,
                                    "FCM token received ✓",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.e("NEXA_FCM", "Token blank")
                                Toast.makeText(
                                    context,
                                    "FCM token empty",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            val msg = task.exception?.message ?: "unknown"
                            Log.e("NEXA_FCM", "Fetch failed: $msg", task.exception)
                            Toast.makeText(
                                context,
                                "FCM fetch failed: $msg",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } catch (e: Exception) {
                Log.e("NEXA_FCM", "Exception fetching token", e)
                Toast.makeText(
                    context,
                    "FCM exception: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(context, "FCM token (cached) ✓", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Step 2: Sync loop (90 attempts = 3 min) ───
    LaunchedEffect(Unit) {
        var attempt = 0
        var lastError = ""

        while (attempt < 90) {
            try {
                val auth = prefs.token
                val fcm = prefs.fcmToken
                val synced = prefs.fcmTokenSynced

                Log.d("NEXA_FCM", "Attempt $attempt: auth=${!auth.isNullOrBlank()}, fcm=${!fcm.isNullOrBlank()}, synced=$synced")

                if (!auth.isNullOrBlank() && !fcm.isNullOrBlank() && !synced) {
                    val res = repo.saveFcmToken(fcm)
                    if (res.isSuccess && res.getOrNull()?.success == true) {
                        prefs.fcmTokenSynced = true
                        Log.d("NEXA_FCM", "Synced ✓")
                        Toast.makeText(
                            context,
                            "FCM synced to server ✓",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@LaunchedEffect
                    } else {
                        lastError = res.exceptionOrNull()?.message ?: "unknown"
                        Log.e("NEXA_FCM", "Sync failed: $lastError")
                        if (attempt == 3) {
                            Toast.makeText(
                                context,
                                "Sync fail: $lastError",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: "exception"
                Log.e("NEXA_FCM", "Loop exception", e)
            }

            delay(2000)
            attempt++
        }

        if (attempt >= 90 && !prefs.fcmTokenSynced) {
            Toast.makeText(
                context,
                "FCM sync timeout. Last: $lastError",
                Toast.LENGTH_LONG
            ).show()
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
