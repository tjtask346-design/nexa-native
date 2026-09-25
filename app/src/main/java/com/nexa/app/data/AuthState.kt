package com.nexa.app.data

object AuthState {
    var pendingEmail: String? = null
    var pendingName: String? = null
    var pinMode: String = "login" // "login" or "setup"
    fun reset() {
        pendingEmail = null
        pendingName = null
        pinMode = "login"
    }
}
