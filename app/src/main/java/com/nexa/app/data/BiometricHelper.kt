package com.nexa.app.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    /**
     * Returns the best authenticator combination the device supports:
     *  - BIOMETRIC_STRONG (highest security — most modern phones)
     *  - BIOMETRIC_WEAK (Class 2 — many Xiaomi/Realme/Poco devices)
     *  - WEAK | DEVICE_CREDENTIAL (fallback, includes device PIN)
     *  - 0 = nothing available
     */
    private fun getBestAuthenticators(ctx: Context): Int {
        val mgr = BiometricManager.from(ctx)
        val strong = BiometricManager.Authenticators.BIOMETRIC_STRONG
        val weak = BiometricManager.Authenticators.BIOMETRIC_WEAK
        val devCred = BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (mgr.canAuthenticate(strong) == BiometricManager.BIOMETRIC_SUCCESS) return strong
        if (mgr.canAuthenticate(weak) == BiometricManager.BIOMETRIC_SUCCESS) return weak
        if (mgr.canAuthenticate(weak or devCred) == BiometricManager.BIOMETRIC_SUCCESS) return (weak or devCred)
        return 0
    }

    /** True if fingerprint/face is actually enrolled */
    fun hasEnrolledBiometric(ctx: Context): Boolean = getBestAuthenticators(ctx) != 0

    /** Legacy alias */
    fun isAvailable(ctx: Context): Boolean = hasEnrolledBiometric(ctx)

    /** Human-readable diagnostic */
    fun getStatusMessage(ctx: Context): String {
        val mgr = BiometricManager.from(ctx)
        val code = mgr.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return when (code) {
            BiometricManager.BIOMETRIC_SUCCESS -> "OK"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No fingerprint sensor on this device"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Fingerprint sensor is unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No fingerprint enrolled in phone Settings"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric not supported"
            else -> "Unknown error (code $code)"
        }
    }

    fun prompt(
        activity: FragmentActivity,
        title: String = "Nexa",
        subtitle: String = "Place your finger on the sensor",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val authenticators = getBestAuthenticators(activity)
        if (authenticators == 0) {
            onError(getStatusMessage(activity))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val msg = when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED -> "cancelled"
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> "No fingerprint enrolled"
                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> "No fingerprint sensor"
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> "Sensor busy, try again"
                    BiometricPrompt.ERROR_LOCKOUT -> "Too many attempts. Wait 30 seconds"
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "Locked. Unlock phone with PIN first"
                    else -> errString.toString()
                }
                onError(msg)
            }
            override fun onAuthenticationFailed() {
                // Silent — user can retry
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        // When DEVICE_CREDENTIAL is included, negative button MUST NOT be set
        val usesDeviceCredential =
            (authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) != 0

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        if (!usesDeviceCredential) {
            builder.setNegativeButtonText("Cancel")
        }

        prompt.authenticate(builder.build())
    }

    fun promptForPin(
        activity: FragmentActivity,
        prefs: Prefs,
        title: String = "Confirm fingerprint",
        subtitle: String = "Place your finger on the sensor",
        onPin: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val saved = prefs.pin
        if (saved == null) {
            onError("No PIN saved. Type it once.")
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
