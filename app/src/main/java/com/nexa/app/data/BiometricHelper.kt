package com.nexa.app.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun isAvailable(ctx: Context): Boolean {
        val mgr = BiometricManager.from(ctx)
        val result = mgr.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(
        activity: FragmentActivity,
        title: String = "Unlock Nexa",
        subtitle: String = "Place your finger",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
            override fun onAuthenticationFailed() {
                onError("Fingerprint not recognized")
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val infoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        prompt.authenticate(infoBuilder.build())
    }

    fun promptForPin(
        activity: FragmentActivity,
        prefs: Prefs,
        title: String = "Confirm with fingerprint",
        subtitle: String = "Place your finger",
        onPin: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val saved = prefs.pin
        if (saved == null) {
            onError("No PIN saved. Please type it once.")
            return
        }
        prompt(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = { onPin(saved) },
            onError = onError
        )
    }
}
