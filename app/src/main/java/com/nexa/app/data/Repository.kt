package com.nexa.app.data

import com.nexa.app.data.ApiClient.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val prefs: Prefs) {

    private fun authHeader(): String = "Bearer ${prefs.token ?: ""}"

    suspend fun register(email: String, fullName: String, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val idToken = FirebaseAuthHelper.ensureUserAndGetIdToken(email)
                api.register(RegisterRequest(idToken, fullName, pin))
            }
        }

    suspend fun loginPin(email: String, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching { api.loginPin(LoginPinRequest(email, pin)) }
        }

    suspend fun me(): Result<MeResponse> = withContext(Dispatchers.IO) {
        runCatching { api.me(authHeader()) }
    }

    suspend fun balance(): Result<BalanceResponse> = withContext(Dispatchers.IO) {
        runCatching { api.balance(authHeader()) }
    }

    suspend fun myTransactions(): Result<TxListResponse> = withContext(Dispatchers.IO) {
        runCatching { api.myTransactions(authHeader()) }
    }

    suspend fun deposit(body: DepositRequest): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching { api.depositRequest(authHeader(), body) }
        }

    suspend fun withdraw(body: WithdrawRequest): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching { api.withdrawRequest(authHeader(), body) }
        }

    fun saveSession(token: String?, user: User?) {
        prefs.token = token
        prefs.email = user?.email
        prefs.name = user?.displayName
    }

    fun savePin(pin: String) { prefs.pin = pin }

    fun clearSession() {
        FirebaseAuthHelper.signOut()
        prefs.clear()
    }
}
