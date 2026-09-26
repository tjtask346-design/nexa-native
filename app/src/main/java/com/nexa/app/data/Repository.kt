package com.nexa.app.data

import com.nexa.app.data.ApiClient.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class Repository(private val prefs: Prefs) {

    private fun authHeader(): String = "Bearer ${prefs.token ?: ""}"

    /** Extracts real error message from Retrofit HttpException */
    private fun errorMessage(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                try {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
                    match?.groupValues?.get(1) ?: "HTTP ${e.code()}"
                } catch (_: Exception) { "HTTP ${e.code()}" }
            }
            else -> e.message ?: e.javaClass.simpleName
        }
    }

    private inline fun <T> wrap(block: () -> T): Result<T> =
        runCatching(block).recoverCatching { throw Exception(errorMessage(it)) }

    suspend fun register(email: String, fullName: String, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap {
                val idToken = FirebaseAuthHelper.ensureUserAndGetIdToken(email)
                api.register(RegisterRequest(idToken, fullName, pin))
            }
        }

    suspend fun loginPin(email: String, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.loginPin(LoginPinRequest(email, pin)) }
        }

    suspend fun me(): Result<MeResponse> = withContext(Dispatchers.IO) {
        wrap { api.me(authHeader()) }
    }

    suspend fun myTransactions(): Result<TxListResponse> = withContext(Dispatchers.IO) {
        wrap { api.myTransactions(authHeader()) }
    }

    suspend fun deposit(amount: Double, trxId: String, methodNumber: String?): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.depositRequest(authHeader(), DepositRequest(amount, trxId, methodNumber)) }
        }

    suspend fun cashout(amount: Double, methodNumber: String?): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.cashoutRequest(authHeader(), CashoutRequest(amount, methodNumber)) }
        }

    suspend fun sendMoney(receiverUid: String, amount: Double, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.sendMoney(authHeader(), SendMoneyRequest(receiverUid, amount, pin)) }
        }

        /* ─── Email verification helpers ─── */
    suspend fun isEmailVerified(): Boolean = FirebaseAuthHelper.isEmailVerified()

    suspend fun resendVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        wrap {
            val ok = FirebaseAuthHelper.resendVerificationEmail()
            if (!ok) throw Exception("No signed-in user to resend to")
        }
    }

    suspend fun currentIdToken(): String? = FirebaseAuthHelper.currentIdToken()

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
