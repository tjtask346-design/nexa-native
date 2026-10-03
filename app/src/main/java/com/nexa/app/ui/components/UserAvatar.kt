package com.nexa.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexa.app.R
import com.nexa.app.data.Prefs
import com.nexa.app.ui.theme.NexaGreen
import com.nexa.app.ui.theme.NexaTeal

/**
 * Universal user avatar.
 *
 * Loads [Prefs.avatarUrl] → user's own picture.
 * If not set → falls back to Nexa logo.
 */
@Composable
fun UserAvatar(
    prefs: Prefs,
    size: Dp,
    cornerRadius: Dp = size / 4,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val avatarUri = prefs.avatarUrl
    val bmp = remember(avatarUri) { loadAvatarBitmap(ctx, avatarUri) }

    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(NexaGreen, NexaTeal)))
    ) {
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
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

private fun loadAvatarBitmap(ctx: Context, uriStr: String?): Bitmap? {
    if (uriStr.isNullOrBlank()) return null
    return try {
        val uri = Uri.parse(uriStr)
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) { null }
}
