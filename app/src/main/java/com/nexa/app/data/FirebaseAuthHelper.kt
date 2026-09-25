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

    suspend fun ensureUserAndGetIdToken(email: String): String {
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
        val tokenResult = user.getIdToken(true).await()
        return tokenResult.token ?: throw IllegalStateException("idToken null")
    }

    fun signOut() { try { auth.signOut() } catch (_: Exception) {} }
}
