package com.nexa.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://nexa-backend-w3xb.onrender.com"

data class User(
    val id: String? = null,
    val _id: String? = null,
    val email: String = "",
    val fullName: String = "",
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val role: String = "user",
    val uid: String? = null
) {
    val displayName: String
        get() = fullName.ifBlank { email.substringBefore("@") }
}

data class AuthResponse(
    val success: Boolean = false,
    val token: String? = null,
    val user: User? = null,
    val message: String? = null
)

data class MeResponse(
    val success: Boolean = false,
    val user: User? = null,
    val message: String? = null
)

data class BalanceResponse(
    val success: Boolean = false,
    val balance: Double = 0.0,
    val message: String? = null
)

data class Transaction(
    val _id: String? = null,
    val id: String? = null,
    val type: String = "",
    val method: String = "",
    val amount: Double = 0.0,
    val status: String = "pending",
    val createdAt: String? = null,
    val transactionId: String? = null
)

data class TxListResponse(
    val success: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val data: List<Transaction> = emptyList(),
    val message: String? = null
)

data class RegisterRequest(
    val idToken: String,
    val fullName: String,
    val pin: String
)

data class LoginPinRequest(
    val email: String,
    val pin: String
)

data class DepositRequest(
    val method: String,
    val amount: Double,
    val trxId: String? = null,
    val senderNumber: String? = null,
    val currency: String? = null
)

data class WithdrawRequest(
    val method: String,
    val amount: Double,
    val destination: String
)

interface NexaApi {
    @POST("/api/auth/register-firebase")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("/api/auth/login-pin")
    suspend fun loginPin(@Body body: LoginPinRequest): AuthResponse

    @GET("/api/auth/me")
    suspend fun me(@Header("Authorization") token: String): MeResponse

    @GET("/api/user/balance")
    suspend fun balance(@Header("Authorization") token: String): BalanceResponse

    @GET("/api/transaction/my")
    suspend fun myTransactions(@Header("Authorization") token: String): TxListResponse

    @POST("/api/transaction/deposit/request")
    suspend fun depositRequest(
        @Header("Authorization") token: String,
        @Body body: DepositRequest
    ): AuthResponse

    @POST("/api/transaction/withdraw/request")
    suspend fun withdrawRequest(
        @Header("Authorization") token: String,
        @Body body: WithdrawRequest
    ): AuthResponse
}

object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: NexaApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NexaApi::class.java)
}
