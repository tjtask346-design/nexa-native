package com.nexa.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.min

object CloudinaryHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    suspend fun uploadKycImage(
        ctx: Context,
        sourceUri: Uri,
        slot: String
    ): String = withContext(Dispatchers.IO) {
        val input = ctx.contentResolver.openInputStream(sourceUri)
            ?: throw Exception("Cannot open image")
        val original = BitmapFactory.decodeStream(input)
        input.close()
            ?: throw Exception("Decode failed")

        val maxDim = 1600
        val scaled: Bitmap = if (original.width > maxDim || original.height > maxDim) {
            val ratio = min(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height)
            Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        } else original

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        val bytes = baos.toByteArray()

        val fileBody = bytes.toRequestBody("image/jpeg".toMediaType())
        val uniqueName = "kyc_${slot}_${UUID.randomUUID()}.jpg"

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", uniqueName, fileBody)
            .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
            .addFormDataPart("folder", "nexa/kyc")
            .build()

        val request = Request.Builder()
            .url(CloudinaryConfig.UPLOAD_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { resp ->
            val responseBody = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                val msg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }
                throw Exception("Upload failed (${resp.code}): ${msg ?: responseBody.take(200)}")
            }
            val json = JSONObject(responseBody)
            val url = json.optString("secure_url")
            if (url.isNullOrEmpty()) throw Exception("No URL in response")
            url
        }
    }
}
