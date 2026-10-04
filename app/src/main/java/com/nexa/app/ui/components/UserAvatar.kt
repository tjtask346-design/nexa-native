package com.nexa.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.nexa.app.R
import com.nexa.app.data.Prefs
import com.nexa.app.ui.theme.NexaGreen
import com.nexa.app.ui.theme.NexaTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// ═══════════════════════════════════════════════
// SHARED BITMAP CACHE
// ═══════════════════════════════════════════════
private val avatarCache = LruCache<String, Bitmap>(30)

@Composable
fun UserAvatar(
    prefs: Prefs,
    size: Dp,
    cornerRadius: Dp = size / 4,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val avatarUri = prefs.avatarUrl

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(avatarUri) {
        bitmap = null
        if (!avatarUri.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                loadAvatarBitmap(ctx, avatarUri)
            }
        }
    }

    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "User avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(R.drawable.nexa_logo),
                contentDescription = "Nexa",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun loadAvatarBitmap(ctx: Context, urlStr: String): Bitmap? {
    // Cache hit
    avatarCache.get(urlStr)?.let { return it }

    return try {
        val bmp = if (urlStr.startsWith("http")) {
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.connect()
            conn.inputStream.use { BitmapFactory.decodeStream(it) }
        } else {
            val uri = Uri.parse(urlStr)
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }

        if (bmp != null) avatarCache.put(urlStr, bmp)
        bmp
    } catch (e: Exception) {
        android.util.Log.e("NEXA_AVATAR", "Load failed: ${e.message}", e)
        null
    }
}
