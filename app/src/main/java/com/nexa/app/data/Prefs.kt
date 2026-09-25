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

    fun clear() = sp.edit().clear().apply()
}
