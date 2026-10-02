package com.nexa.app.nav

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.screens.*
import com.nexa.app.ui.theme.NexaBg
import com.nexa.app.ui.theme.NexaGreen
import com.nexa.app.ui.theme.NexaTeal

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

private val NexaEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val ANIM_MS = 320

@Composable
fun NexaNav(prefs: Prefs, repo: Repository) {
    val nav = rememberNavController()

    Box(
        Modifier
            .fillMaxSize()
            .background(NexaBg)
    ) {
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

            // ═══════════════════════════════════════════════
            // SPLASH — validates session before navigating
            // ═══════════════════════════════════════════════
            composable(Routes.SPLASH) {
                var splashTimePassed by remember { mutableStateOf(false) }
                var validationDone by remember { mutableStateOf(false) }
                var destination by remember { mutableStateOf<String?>(null) }

                // Validate token with backend
                LaunchedEffect(Unit) {
                    val savedToken = prefs.token

                    if (!savedToken.isNullOrBlank()) {
                        // Ask backend: is this session still valid?
                        val res = repo.me()

                        if (res.isSuccess && res.getOrNull()?.success == true) {
                            // Session valid → go to PIN
                            destination = Routes.PIN
                        } else {
                            // User deleted OR token expired → force logout
                            try { repo.clearSession() } catch (_: Exception) { }
                            destination = Routes.LOGIN
                        }
                    } else {
                        destination = Routes.LOGIN
                    }
                    validationDone = true
                }

                // Navigate only after BOTH splash delay AND validation finish
                LaunchedEffect(splashTimePassed, validationDone, destination) {
                    if (splashTimePassed && validationDone && destination != null) {
                        nav.navigate(destination!!) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }

                SplashScreen {
                    splashTimePassed = true
                }
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
            composable(Routes.PROFILE)       { ProfileScreen(nav, prefs) }

            composable(Routes.DEPOSIT)       { DepositScreen(nav, repo) }
            composable(Routes.WITHDRAW)      { WithdrawScreen(nav, prefs, repo) }
            composable(Routes.MYQR)          { MyQrScreen(nav, prefs) }
            composable(Routes.SCAN)          { ScanScreen(nav, prefs) }
            composable(Routes.SEND)          { SendScreen(nav, prefs, repo) }
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
    }
}
