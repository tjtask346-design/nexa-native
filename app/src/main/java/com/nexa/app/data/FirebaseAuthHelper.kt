package com.nexa.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

object FirebaseAuthHelper {
    private val auth: FirebaseAuth get() = Firebase.auth

    private fun derivePassword(email: String): String {
        val bytes = (email.lowercase().trim() + "|nexa-app-salt-v1").toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return "Nx" + digest.joinToString("") { "%02x".format(it) }.take(30)
    }

    /** Create Firebase user + send verification email, return idToken */
    suspend fun createUserAndSendVerification(email: String): String {
        val normalized = email.lowercase().trim()
        val password = derivePassword(normalized)

        try {
            auth.createUserWithEmailAndPassword(normalized, password).await()
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("already in use", true) ||
                msg.contains("email-already-exists", true) ||
                msg.contains("EMAIL_EXISTS", true)) {
                try {
                    auth.signInWithEmailAndPassword(normalized, password).await()
                } catch (e2: Exception) {
                    throw IllegalStateException("Firebase sign-in failed: ${e2.message ?: "unknown"}")
                }
            } else {
                throw IllegalStateException("Firebase: ${e.message ?: "unknown"}")
            }
        }

        val user = auth.currentUser ?: throw IllegalStateException("Firebase user null")

        try {
            if (!user.isEmailVerified) {
                user.sendEmailVerification().await()
            }
        } catch (_: Exception) { }

        val tokenResult = user.getIdToken(true).await()
        return tokenResult.token ?: throw IllegalStateException("idToken null")
    }

    /** Resend verification email to current user */
    suspend fun resendVerificationEmail(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            if (!user.isEmailVerified) {
                user.sendEmailVerification().await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Check if current Firebase user has verified email */
    suspend fun isEmailVerified(): Boolean {
        val user = auth.currentUser ?: return false
        try { user.reload().await() } catch (_: Exception) { }
        return user.isEmailVerified
    }

    /** Get fresh idToken for current user */
    suspend fun currentIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(true)?.await()?.token
        } catch (_: Exception) { null }
    }

    fun signOut() { try { auth.signOut() } catch (_: Exception) {} }
}
