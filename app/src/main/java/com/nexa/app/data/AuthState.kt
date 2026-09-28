package com.nexa.app.data

object AuthState {
    var pendingEmail: String? = null
    var pendingName: String? = null
    var pinMode: String = "login" // "login" | "setup" | "confirm"

    /**
     * JWT token obtained after:
     *  - register (post email verify), or
     *  - login-pin when TOTP not yet enabled
     * Used to authenticate /setup-totp call.
     */
    var pendingToken: String? = null

    fun reset() {
        pendingEmail = null
        pendingName = null
        pinMode = "login"
        pendingToken = null
    }
}
