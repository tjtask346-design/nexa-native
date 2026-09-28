package com.nexa.app.data

object AuthState {
    var pendingEmail: String? = null
    var pendingName: String? = null
    var pendingToken: String? = null
    var pinMode: String = "login"
    fun reset() { pendingEmail = null; pendingName = null; pendingToken = null; pinMode = "login" }
}
