package com.fthertz.sigmastore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class AppInfo(
    val id: Int,
    val app_name: String,
    val icon_url: String?,
    val icon_working: Boolean,
    val description: String,
    val category: String,
    val developer: String,
    val age_rating: String,
    val screenshots: List<String>,
    val is_real_apk: Boolean,
    val apk_file: String
)

object AppRepository {

    fun loadAppsFromAssets(context: Context): List<AppInfo> {
        return try {
            val json = context.assets.open("dataset.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<AppInfo>>() {}.type
            val apps = Gson().fromJson<List<AppInfo>>(json, type) ?: emptyList()
            apps
        } catch (e: Exception) {
            println("Ошибка загрузки из assets: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // Остальной код остается без изменений
    suspend fun getImage(imageName: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val imageRequest = ImageRequest(image_name = "tetris_1.webp")
            val jsonBody =
                Gson().toJson(imageRequest).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/get_image")
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = OkHttpClient().newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.byteStream()?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Исключение при загрузке $imageName: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}