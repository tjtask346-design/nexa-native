package com.nexa.app.data

import com.nexa.app.data.ApiClient.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class Repository(private val prefs: Prefs) {
    private fun authHeader() = "Bearer ${prefs.token ?: ""}"
    private fun err(e: Throwable): String = when (e) {
        is HttpException -> try {
            val b = e.response()?.errorBody()?.string() ?: ""
            Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(b)?.groupValues?.get(1) ?: "HTTP ${e.code()}"
        } catch (_: Exception) { "HTTP ${e.code()}" }
        else -> e.message ?: e.javaClass.simpleName
    }
    private inline fun <T> wrap(block: () -> T): Result<T> =
        runCatching(block).recoverCatching { throw Exception(err(it)) }

    suspend fun createFirebaseUser(email: String): Result<String> = withContext(Dispatchers.IO) {
        wrap { FirebaseAuthHelper.createUserAndSendVerification(email) }
    }
    suspend fun isEmailVerified() = FirebaseAuthHelper.isEmailVerified()
    suspend fun resendVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        wrap {
            val email = prefs.email ?: throw Exception("No email saved")
            FirebaseAuthHelper.createUserAndSendVerification(email)
            Unit
        }
    }
    suspend fun register(email: String, fullName: String, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            wrap {
                val idToken = FirebaseAuthHelper.currentIdToken()
                    ?: FirebaseAuthHelper.createUserAndSendVerification(email)
                api.register(RegisterRequest(idToken, fullName, pin))
            }
        }
    suspend fun loginPin(email: String, pin: String, code: String? = null): Result<AuthResponse> =
        withContext(Dispatchers.IO) { wrap { api.loginPin(LoginPinRequest(email, pin, code)) } }

    suspend fun setupTotp(token: String): Result<SetupTotpResponse> = withContext(Dispatchers.IO) {
        wrap { api.setupTotp("Bearer $token") }
    }
    suspend fun verifyTotpSetup(token: String, code: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.verifyTotpSetup("Bearer $token", VerifyTotpRequest(code)) } }
    suspend fun disableTotp(pin: String, code: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.disableTotp(authHeader(), DisableTotpRequest(pin, code)) } }
    suspend fun resetPinWithTotp(email: String, code: String, newPin: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.resetPinWithTotp(ResetPinRequest(email, code, newPin)) } }

    suspend fun me(): Result<MeResponse> = withContext(Dispatchers.IO) { wrap { api.me(authHeader()) } }
    suspend fun myTransactions(): Result<TxListResponse> = withContext(Dispatchers.IO) { wrap { api.myTransactions(authHeader()) } }
    suspend fun deposit(amount: Double, trxId: String, methodNumber: String?): Result<AuthResponse> =
        withContext(Dispatchers.IO) { wrap { api.depositRequest(authHeader(), DepositRequest(amount, trxId, methodNumber)) } }
    suspend fun cashout(amount: Double, methodNumber: String?): Result<AuthResponse> =
        withContext(Dispatchers.IO) { wrap { api.cashoutRequest(authHeader(), CashoutRequest(amount, methodNumber)) } }
    suspend fun sendMoney(receiverUid: String, amount: Double, pin: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) { wrap { api.sendMoney(authHeader(), SendMoneyRequest(receiverUid, amount, pin)) } }
    suspend fun withdrawCrypto(toAddress: String, amount: Double): Result<SimpleResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.withdrawOnchain(authHeader(), WithdrawOnchainRequest(toAddress, amount)) }
        }

    suspend fun submitKyc(nid: String, front: String, back: String, selfie: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.submitKyc(authHeader(), KycSubmitRequest(nid, front, back, selfie)) } }
    suspend fun myKyc(): Result<KycListResponse> = withContext(Dispatchers.IO) { wrap { api.myKyc(authHeader()) } }

    suspend fun saveFcmToken(fcmToken: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) {
            wrap { api.saveFcmToken(authHeader(), FcmTokenRequest(fcmToken)) }
        }

    suspend fun getNotifications(): Result<NotificationsResponse> =
        withContext(Dispatchers.IO) { wrap { api.getNotifications(authHeader()) } }
    suspend fun markNotificationRead(id: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.markNotificationRead(authHeader(), id) } }
    suspend fun markAllNotificationsRead(): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.markAllNotificationsRead(authHeader()) } }
    suspend fun deleteNotification(id: String): Result<SimpleResponse> =
        withContext(Dispatchers.IO) { wrap { api.deleteNotification(authHeader(), id) } }

    fun saveSession(token: String?, user: User?) {
        prefs.token = token
        prefs.email = user?.email
        prefs.name = user?.displayName
        prefs.accountNumber = user?.accountNumber
        prefs.fcmTokenSynced = false
    }
    fun savePin(pin: String) { prefs.pin = pin }
    fun clearSession() {
        FirebaseAuthHelper.signOut()
        prefs.logout()   // ← preserves avatar + fcm token + account number
    }
}
