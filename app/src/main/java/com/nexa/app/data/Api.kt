package com.nexa.app.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://nexa-backend-w3xb.onrender.com"

object NexaConfig {
    const val BDT_RATE = 122.0
    const val ADMIN_BKASH_NUMBER = "01772277956"
    const val ADMIN_BKASH_NAME = "Nexa Admin"
    const val MIN_DEPOSIT_USD = 10.0
    const val MIN_WITHDRAW_USD = 20.0
}

data class Wallets(
    val bscAddress: String? = null,
    val walletIndex: Int? = null,
    val xpub: String? = null
)

data class User(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String? = null,
    val email: String = "",
    val fullName: String = "",
    val accountNumber: String = "",
    val role: String = "user",
    val balance: Double = 0.0,
    val uid: String? = null,
    val kycStatus: String = "unverified",
    val totpEnabled: Boolean = false,
    val wallets: Wallets? = null,
    val ltcAddress: String? = null
) {
    val displayName: String get() = fullName.ifBlank { email.substringBefore("@") }
    val handle: String get() = "@" + email.substringBefore("@").lowercase()
}

data class AuthResponse(
    val success: Boolean = false,
    val token: String? = null,
    val user: User? = null,
    val message: String? = null,
    val requiresTotp: Boolean = false,
    val requiresTotpSetup: Boolean = false
)

data class MeResponse(val success: Boolean = false, val user: User? = null, val message: String? = null)

data class Transaction(
    @SerializedName(value = "_id", alternate = ["id"])
    val id: String? = null,
    val type: String = "",
    val amount: Double = 0.0,
    val status: String = "pending",
    val trxId: String? = null,
    val senderUid: String? = null,
    val receiverUid: String? = null,
    val paymentMethodNumber: String? = null,
    val createdAt: String? = null
)

data class TxListResponse(
    val success: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val count: Int = 0,
    val message: String? = null
)

data class KycSubmission(
    @SerializedName(value = "_id", alternate = ["id"])
    val id: String? = null,
    val user: User? = null,
    val nidNumber: String = "",
    val frontUrl: String = "",
    val backUrl: String = "",
    val selfieUrl: String = "",
    val status: String = "pending",
    val adminNote: String? = null,
    val createdAt: String? = null
)

data class KycListResponse(
    val success: Boolean = false,
    val kycs: List<KycSubmission> = emptyList(),
    val count: Int = 0,
    val kyc: KycSubmission? = null,
    val message: String? = null
)

data class KycSubmitRequest(val nidNumber: String, val frontUrl: String, val backUrl: String, val selfieUrl: String)
data class SimpleResponse(val success: Boolean = false, val message: String? = null)
data class RegisterRequest(val idToken: String, val fullName: String, val pin: String)
data class LoginPinRequest(val email: String, val pin: String, val code: String? = null)
data class DepositRequest(val amount: Double, val trxId: String, val paymentMethodNumber: String? = null)
data class CashoutRequest(val amount: Double, val paymentMethodNumber: String? = null)
data class WithdrawOnchainRequest(val to: String, val amount: Double)

data class SendMoneyRequest(val receiverUid: String, val amount: Double, val pin: String)

data class SetupTotpResponse(
    val success: Boolean = false,
    val secret: String? = null,
    val otpauth: String? = null,
    val message: String? = null
)
data class VerifyTotpRequest(val code: String)
data class DisableTotpRequest(val pin: String, val code: String)
data class ResetPinRequest(val email: String, val code: String, val newPin: String)

interface NexaApi {
    @POST("/api/auth/register-firebase")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("/api/auth/login-pin")
    suspend fun loginPin(@Body body: LoginPinRequest): AuthResponse

    @GET("/api/auth/me")
    suspend fun me(@Header("Authorization") token: String): MeResponse

    @POST("/api/auth/setup-totp")
    suspend fun setupTotp(@Header("Authorization") token: String): SetupTotpResponse

    @POST("/api/auth/verify-totp-setup")
    suspend fun verifyTotpSetup(@Header("Authorization") token: String, @Body body: VerifyTotpRequest): SimpleResponse

    @POST("/api/auth/disable-totp")
    suspend fun disableTotp(@Header("Authorization") token: String, @Body body: DisableTotpRequest): SimpleResponse

    @POST("/api/auth/reset-pin-with-totp")
    suspend fun resetPinWithTotp(@Body body: ResetPinRequest): SimpleResponse

    @GET("/api/transaction/my")
    suspend fun myTransactions(@Header("Authorization") token: String): TxListResponse

    @POST("/api/transaction/deposit/request")
    suspend fun depositRequest(@Header("Authorization") token: String, @Body body: DepositRequest): AuthResponse

    @POST("/api/transaction/cashout/request")
    suspend fun cashoutRequest(@Header("Authorization") token: String, @Body body: CashoutRequest): AuthResponse

    @POST("/api/tatum/withdraw/onchain")
    suspend fun withdrawOnchain(
        @Header("Authorization") token: String,
        @Body body: WithdrawOnchainRequest
    ): SimpleResponse

    @POST("/api/transaction/send")
    suspend fun sendMoney(@Header("Authorization") token: String, @Body body: SendMoneyRequest): AuthResponse

    @POST("/api/kyc/submit")
    suspend fun submitKyc(@Header("Authorization") token: String, @Body body: KycSubmitRequest): SimpleResponse

    @GET("/api/kyc/my")
    suspend fun myKyc(@Header("Authorization") token: String): KycListResponse
}

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()
    val api: NexaApi = Retrofit.Builder()
        .baseUrl(BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(NexaApi::class.java)
}
