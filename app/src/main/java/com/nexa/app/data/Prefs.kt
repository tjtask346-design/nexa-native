package com.nexa.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Prefs(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sp = EncryptedSharedPreferences.create(
        context,
        "nexa_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var token: String?
        get() = sp.getString("token", null)
        set(v) = sp.edit().putString("token", v).apply()

    var email: String?
        get() = sp.getString("email", null)
        set(v) = sp.edit().putString("email", v).apply()

    var name: String?
        get() = sp.getString("name", null)
        set(v) = sp.edit().putString("name", v).apply()

    var pin: String?
        get() = sp.getString("pin", null)
        set(v) = sp.edit().putString("pin", v).apply()

    var biometricEnabled: Boolean
        get() = sp.getBoolean("biometric", false)
        set(v) = sp.edit().putBoolean("biometric", v).apply()

    var avatarUrl: String?
        get() = sp.getString("avatarUrl", null)
        set(v) = sp.edit().putString("avatarUrl", v).apply()

    var fcmToken: String?
        get() = sp.getString("fcmToken", null)
        set(v) = sp.edit().putString("fcmToken", v).apply()

    var fcmTokenSynced: Boolean
        get() = sp.getBoolean("fcmTokenSynced", false)
        set(v) = sp.edit().putBoolean("fcmTokenSynced", v).apply()

    var accountNumber: String?
        get() = sp.getString("accountNumber", null)
        set(v) = sp.edit().putString("accountNumber", v).apply()

    // ═══ NEW: KYC status cache ═══
    var kycStatus: String
        get() = sp.getString("kycStatus", "unverified") ?: "unverified"
        set(v) = sp.edit().putString("kycStatus", v).apply()

    fun clear() = sp.edit().clear().apply()

    fun logout() {
        val savedAvatar = avatarUrl
        val savedFcm = fcmToken
        val savedAccount = accountNumber
        val savedKyc = kycStatus
        sp.edit().clear().apply()
        avatarUrl = savedAvatar
        fcmToken = savedFcm
        accountNumber = savedAccount
        kycStatus = savedKyc
    }
}
