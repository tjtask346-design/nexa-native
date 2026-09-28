package com.nexa.app.nav

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.screens.*

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
}

private val Ease = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@Composable
fun NexaNav(prefs: Prefs, repo: Repository) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally({ it / 6 }, tween(320, easing = Ease)) + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { slideOutHorizontally({ it / 6 }, tween(280, easing = Ease)) + fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) { SplashScreen {
            val next = if (prefs.token != null) Routes.PIN else Routes.LOGIN
            nav.navigate(next) { popUpTo(Routes.SPLASH) { inclusive = true } }
        } }
        composable(Routes.LOGIN)        { LoginScreen(nav, prefs) }
        composable(Routes.SIGNUP)       { SignupScreen(nav, prefs) }
        composable(Routes.PIN)          { PinScreen(nav, prefs, repo) }
        composable(Routes.VERIFY_EMAIL) { VerifyEmailScreen(nav, prefs, repo) }
        composable(Routes.FORGOT_PIN)   { ForgotPinScreen(nav, prefs) }
        composable(Routes.SETUP_TOTP)   { SetupTotpScreen(nav, prefs, repo) }
        composable(Routes.VERIFY_TOTP)  { VerifyTotpScreen(nav, prefs, repo) }
        composable(Routes.RESET_PIN)    { ResetPinScreen(nav, prefs, repo) }
        composable(Routes.HOME)         { HomeScreen(nav, prefs, repo) }
        composable(Routes.HISTORY)      { HistoryScreen(nav, prefs, repo) }
        composable(Routes.PROFILE)      { ProfileScreen(nav, prefs) }
        composable(Routes.DEPOSIT)      { DepositScreen(nav, repo) }
        composable(Routes.WITHDRAW)     { WithdrawScreen(nav, prefs, repo) }
        composable(Routes.MYQR)         { MyQrScreen(nav, prefs) }
        composable(Routes.SCAN)         { ScanScreen(nav, prefs) }
        composable(Routes.SEND)         { SendScreen(nav, prefs, repo) }
        composable(Routes.KYC)          { KycScreen(nav, prefs, repo) }
        composable(Routes.SUCCESS + "?kind={kind}&amount={amount}&id={id}",
            arguments = listOf(
                navArgument("kind") { type = NavType.StringType; defaultValue = "deposit" },
                navArgument("amount") { type = NavType.StringType; defaultValue = "0" },
                navArgument("id") { type = NavType.StringType; defaultValue = "—" }
            )) { back ->
            SuccessScreen(nav, back.arguments?.getString("kind") ?: "deposit",
                back.arguments?.getString("amount") ?: "0", back.arguments?.getString("id") ?: "—")
        }
    }
}
