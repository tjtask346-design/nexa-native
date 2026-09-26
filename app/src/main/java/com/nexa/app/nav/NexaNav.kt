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
    const val HOME = "home"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val DEPOSIT = "deposit"
    const val WITHDRAW = "withdraw"
    const val MYQR = "myqr"
    const val SCAN = "scan"
    const val SUCCESS = "success"
}

private val NexaEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@Composable
fun NexaNav(prefs: Prefs, repo: Repository) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideInHorizontally({ it / 6 }, tween(320, easing = NexaEase)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = {
            slideOutHorizontally({ it / 6 }, tween(280, easing = NexaEase)) + fadeOut(tween(200))
        }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                val next = if (prefs.token != null) Routes.PIN else Routes.LOGIN
                nav.navigate(next) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }
        composable(Routes.LOGIN)  { LoginScreen(nav, prefs) }
        composable(Routes.SIGNUP) { SignupScreen(nav, prefs) }
        composable(Routes.PIN)    { PinScreen(nav, prefs, repo) }

        composable(Routes.HOME)    { HomeScreen(nav, prefs, repo) }
        composable(Routes.HISTORY) { HistoryScreen(nav, repo) }
        composable(Routes.PROFILE) { ProfileScreen(nav, prefs) }

        composable(Routes.DEPOSIT)  { DepositScreen(nav, repo) }
        composable(Routes.WITHDRAW) { WithdrawScreen(nav, repo) }
        composable(Routes.MYQR)     { MyQrScreen(nav, prefs) }

        // SCAN → route to MyQr placeholder for now (real scanner comes in later step)
        composable(Routes.SCAN) { MyQrScreen(nav, prefs) }

        composable(
            route = Routes.SUCCESS + "?kind={kind}&amount={amount}&id={id}",
            arguments = listOf(
                navArgument("kind")   { type = NavType.StringType; defaultValue = "deposit" },
                navArgument("amount") { type = NavType.StringType; defaultValue = "0" },
                navArgument("id")     { type = NavType.StringType; defaultValue = "—" }
            )
        ) { back ->
            SuccessScreen(
                nav = nav,
                kind = back.arguments?.getString("kind") ?: "deposit",
                amount = back.arguments?.getString("amount") ?: "0",
                id = back.arguments?.getString("id") ?: "—"
            )
        }
    }
}
