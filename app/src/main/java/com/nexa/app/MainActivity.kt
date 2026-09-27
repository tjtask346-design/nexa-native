package com.nexa.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nexa.app.data.Prefs
import com.nexa.app.data.Repository
import com.nexa.app.nav.NexaNav
import com.nexa.app.ui.theme.NexaBg
import com.nexa.app.ui.theme.NexaTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)
        val repo = Repository(prefs)
        setContent {
            NexaTheme {
                Surface(color = NexaBg, modifier = Modifier.fillMaxSize()) {
                    NexaNav(prefs, repo)
                }
            }
        }
    }
}
