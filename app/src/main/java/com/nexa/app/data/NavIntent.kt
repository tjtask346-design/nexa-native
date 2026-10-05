package com.nexa.app.data

object NavIntent {
    var targetScreen: String? = null
    var txId: String? = null
    var type: String? = null

    fun clear() {
        targetScreen = null
        txId = null
        type = null
    }
}
