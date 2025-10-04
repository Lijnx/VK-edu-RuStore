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
import java.util.concurrent.TimeUnit

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

// Временный класс для парсинга текущего JSON
data class TempAppInfo(
    val id: Int,
    val name: String,
    val package_name: String,
    val category: String,
    val download_count: Int,
    val is_real_apk: String
)

object AppRepository {
    private const val BASE_URL = "http://10.209.1.186:8000"

    fun loadAppsFromAssets(context: Context): List<AppInfo> {
        return try {
            val json = context.assets.open("AppsDataset.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<TempAppInfo>>() {}.type
            val tempApps = Gson().fromJson<List<TempAppInfo>>(json, type) ?: emptyList()

            // Преобразуем TempAppInfo в AppInfo
            tempApps.map { tempApp ->
                AppInfo(
                    id = tempApp.id,
                    app_name = tempApp.name ?: "Неизвестное приложение",
                    //icon_url = "$BASE_URL/images/${tempApp.package_name}_icon.webp",
                    icon_url = "$BASE_URL/get_image",
                    icon_working = true,
                    description = "Описание для ${tempApp.name}",
                    category = tempApp.category ?: "Другое",
                    developer = "Неизвестный разработчик",
                    age_rating = "12+",
                    screenshots = listOf("${tempApp.package_name}_screen1.webp"),
                    is_real_apk = tempApp.is_real_apk == "True",
                    apk_file = "${tempApp.package_name}.apk"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Остальной код остается без изменений
    suspend fun getImage(imageName: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val imageRequest = ImageRequest(image_name = "tetris_2.webp")
            val jsonBody = Gson().toJson(imageRequest).toRequestBody("application/json".toMediaType())

            // ⚠️ Меняем URL на /get_image и убеждаемся что это POST
            val request = Request.Builder()
                .url("$BASE_URL/get_image") // ⚠️ Правильный endpoint
                .post(jsonBody) // ⚠️ Явно указываем POST
                .addHeader("Content-Type", "application/json")
                .build()

            println("🖼️ Отправка POST запроса на: $BASE_URL/get_image")
            println("🖼️ Тело запроса: ${Gson().toJson(imageRequest)}")

            val response = OkHttpClient().newCall(request).execute()

            println("🖼️ Ответ сервера для $imageName: ${response.code} - ${response.message}")

            if (response.isSuccessful) {
                response.body?.byteStream()?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                println("Ошибка сервера для $imageName: ${response.code}")
                null
            }
        } catch (e: Exception) {
            println("Исключение при загрузке $imageName: ${e.message}")
            e.printStackTrace()
            null
        }
    }

//    suspend fun getImage(imageName: String): Bitmap? = withContext(Dispatchers.IO) {
//        try {
//            val imageRequest = ImageRequest(name = imageName)
//            val jsonBody = Gson().toJson(imageRequest).toRequestBody("application/json".toMediaType())
//
//            val request = Request.Builder()
//                .url(BASE_URL)
//                .post(jsonBody)
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            val response = OkHttpClient().newCall(request).execute()
//
//            if (response.isSuccessful) {
//                response.body?.byteStream()?.use { inputStream ->
//                    BitmapFactory.decodeStream(inputStream)
//                }
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}