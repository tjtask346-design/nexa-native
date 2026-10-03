package com.nexa.app.nav

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.screens.*
import com.nexa.app.ui.theme.*
import kotlinx.coroutines.delay

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PIN = "pin"
    const val VERIFY_EMAIL = "verify_email"
    const val FORGOT_PIN = "forgot_pin"
    const val SETUP_TOTP = "setup_totp"
    const val VERIFY_TOTP = "verify_totp"
    const val RESET_PIN = "reset_pin"
    const val HOME = "home"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val DEPOSIT = "deposit"
    const val WITHDRAW = "withdraw"
    const val MYQR = "myqr"
    const val SCAN = "scan"
    const val SEND = "send"
    const val SUCCESS = "success"
    const val KYC = "kyc"
    const val NOTIFICATIONS = "notifications"
}

// Screens that need KYC verification
private val KYC_REQUIRED_ROUTES = setOf(
    Routes.HOME, Routes.HISTORY, Routes.PROFILE,
    Routes.DEPOSIT, Routes.WITHDRAW, Routes.MYQR,
    Routes.SCAN, Routes.SEND, Routes.NOTIFICATIONS, Routes.SUCCESS
)

// Screens that should NOT be blocked by offline wall (TOTP copy-paste flow)
private val OFFLINE_EXEMPT = setOf(
    Routes.LOGIN, Routes.SIGNUP, Routes.PIN,
    Routes.VERIFY_EMAIL, Routes.FORGOT_PIN,
    Routes.SETUP_TOTP, Routes.VERIFY_TOTP,
    Routes.RESET_PIN, Routes.KYC
)

private val NexaEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val ANIM_MS = 320

@Composable
fun NexaNav(prefs: Prefs, repo: Repository) {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentPath = backStackEntry?.destination?.route?.substringBefore("?") ?: ""

    // ═══ KYC status — cached + polled ═══
    var kycStatus by remember { mutableStateOf(prefs.kycStatus) }

    // Poll /me every 5 seconds while logged in
    LaunchedEffect(Unit) {
        while (true) {
            if (!prefs.token.isNullOrBlank()) {
                repo.me().onSuccess { res ->
                    res.user?.kycStatus?.let { status ->
                        if (status != kycStatus) {
                            kycStatus = status
                            prefs.kycStatus = status
                        }
                    }
                }
            }
            delay(5000)
        }
    }

    // ═══ KYC Force: redirect non-verified users to KYC screen ═══
    LaunchedEffect(currentPath, kycStatus) {
        val loggedIn = !prefs.token.isNullOrBlank()
        if (loggedIn && currentPath in KYC_REQUIRED_ROUTES && kycStatus != "verified") {
            nav.navigate(Routes.KYC) {
                popUpTo(Routes.KYC) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // ═══ Network state ═══
    var online by remember { mutableStateOf(isNetworkOnline(ctx)) }

    DisposableEffect(Unit) {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { online = true }
            override fun onLost(network: Network) { online = isNetworkOnline(ctx) }
            override fun onUnavailable() { online = false }
        }
        try { cm.registerDefaultNetworkCallback(cb) } catch (_: Exception) { }
        onDispose {
            try { cm.unregisterNetworkCallback(cb) } catch (_: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = isNetworkOnline(ctx)
            if (now != online) online = now
            delay(2000)
        }
    }

    val offlineExempt = currentPath in OFFLINE_EXEMPT
    val showOfflineWall = !online && !offlineExempt

    Box(Modifier.fillMaxSize().background(NexaBg)) {
        // Ambient glow
        Canvas(Modifier.fillMaxSize()) {
            val gC = Offset(0f, 0f)
            val gR = size.width * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to NexaGreen.copy(alpha = 0.22f),
                        0.68f to Color.Transparent
                    ),
                    center = gC, radius = gR
                ),
                radius = gR, center = gC
            )
            val tC = Offset(size.width, size.height * 0.14f)
            val tR = size.width * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to NexaTeal.copy(alpha = 0.16f),
                        0.70f to Color.Transparent
                    ),
                    center = tC, radius = tR
                ),
                radius = tR, center = tC
            )
        }

        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(ANIM_MS, easing = NexaEase),
                    initialOffsetX = { it / 6 }
                ) + fadeIn(animationSpec = tween(ANIM_MS))
            },
            exitTransition = { fadeOut(animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(280, easing = NexaEase),
                    targetOffsetX = { it / 6 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(Routes.SPLASH) {
                var splashTimePassed by remember { mutableStateOf(false) }
                var validationDone by remember { mutableStateOf(false) }
                var destination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val savedToken = prefs.token
                    if (!savedToken.isNullOrBlank()) {
                        if (!online) {
                            destination = Routes.PIN
                        } else {
                            val res = repo.me()
                            if (res.isSuccess && res.getOrNull()?.success == true) {
                                res.getOrNull()?.user?.kycStatus?.let {
                                    kycStatus = it
                                    prefs.kycStatus = it
                                }
                                destination = Routes.PIN
                            } else {
                                try { repo.clearSession() } catch (_: Exception) { }
                                destination = Routes.LOGIN
                            }
                        }
                    } else {
                        destination = Routes.LOGIN
                    }
                    validationDone = true
                }

                LaunchedEffect(splashTimePassed, validationDone, destination) {
                    if (splashTimePassed && validationDone && destination != null) {
                        nav.navigate(destination!!) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }

                SplashScreen { splashTimePassed = true }
            }

            composable(Routes.LOGIN)         { LoginScreen(nav, prefs) }
            composable(Routes.SIGNUP)        { SignupScreen(nav, prefs) }
            composable(Routes.PIN)           { PinScreen(nav, prefs, repo) }
            composable(Routes.VERIFY_EMAIL)  { VerifyEmailScreen(nav, prefs, repo) }
            composable(Routes.FORGOT_PIN)    { ForgotPinScreen(nav, prefs) }
            composable(Routes.SETUP_TOTP)    { SetupTotpScreen(nav, prefs, repo) }
            composable(Routes.VERIFY_TOTP)   { VerifyTotpScreen(nav, prefs, repo) }
            composable(Routes.RESET_PIN)     { ResetPinScreen(nav, prefs, repo) }

            composable(Routes.HOME)          { HomeScreen(nav, prefs, repo) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(nav, repo) }
            composable(Routes.HISTORY)       { HistoryScreen(nav, prefs, repo) }
            composable(Routes.PROFILE)       { ProfileScreen(nav, prefs, repo) }

            composable(
                route = Routes.DEPOSIT + "?crypto={crypto}",
                arguments = listOf(
                    navArgument("crypto") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                DepositScreen(
                    nav = nav,
                    repo = repo,
                    initialCrypto = entry.arguments?.getString("crypto") ?: ""
                )
            }

            composable(Routes.WITHDRAW)      { WithdrawScreen(nav, prefs, repo) }
            composable(Routes.MYQR)          { MyQrScreen(nav, prefs) }
            composable(Routes.SCAN)          { ScanScreen(nav, prefs) }

            composable(
                route = Routes.SEND + "?recipient={recipient}",
                arguments = listOf(
                    navArgument("recipient") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                SendScreen(
                    nav = nav,
                    prefs = prefs,
                    repo = repo,
                    prefilledRecipient = entry.arguments?.getString("recipient") ?: ""
                )
            }

            composable(Routes.KYC)           { KycScreen(nav, prefs, repo) }

            composable(
                route = Routes.SUCCESS + "?kind={kind}&amount={amount}&id={id}",
                arguments = listOf(
                    navArgument("kind")   { type = NavType.StringType; defaultValue = "deposit" },
                    navArgument("amount") { type = NavType.StringType; defaultValue = "0" },
                    navArgument("id")     { type = NavType.StringType; defaultValue = "—" }
                )
            ) { entry ->
                SuccessScreen(
                    nav = nav,
                    kind = entry.arguments?.getString("kind") ?: "deposit",
                    amount = entry.arguments?.getString("amount") ?: "0",
                    id = entry.arguments?.getString("id") ?: "—"
                )
            }
        }

        if (showOfflineWall) {
            OfflineWall(ctx)
        }
    }
}

@Composable
private fun OfflineWall(context: Context) {
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
