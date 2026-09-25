package com.nexa.app.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PIN = "pin"
    const val HOME = "home"
    const val DEPOSIT = "deposit"
    const val WITHDRAW = "withdraw"
    const val MYQR = "myqr"
    const val HISTORY = "history"
    const val PROFILE = "profile"
}

@Composable
fun NexaNav(prefs: Prefs, repo: Repository) {
    val nav = rememberNavController()
    val start = if (prefs.token != null) Routes.PIN else Routes.LOGIN

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen {
                nav.navigate(start) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
        composable(Routes.LOGIN) { LoginScreen(nav, prefs) }
        composable(Routes.SIGNUP) { SignupScreen(nav, prefs) }
        composable(Routes.PIN) { PinScreen(nav, prefs, repo) }
        composable(Routes.HOME) { HomeScreen(nav, prefs, repo) }
        composable(Routes.DEPOSIT) { DepositScreen(nav, repo) }
        composable(Routes.WITHDRAW) { WithdrawScreen(nav, repo) }
        composable(Routes.MYQR) { MyQrScreen(nav, prefs) }
        composable(Routes.HISTORY) { HistoryScreen(nav, repo) }
        composable(Routes.PROFILE) { ProfileScreen(nav, prefs) }
    }
}
