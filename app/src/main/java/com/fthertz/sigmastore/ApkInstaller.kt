package com.fthertz.sigmastore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object ApkInstaller {

    private const val APK_DIRECTORY = "downloaded_apks"

    // Запрос разрешения на установку из неизвестных источников
    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback - открываем настройки приложения
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }

    // Проверка наличия разрешения
    fun hasInstallPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Для версий ниже Android 8.0 разрешение не требуется
        }
    }

    suspend fun downloadAndInstallApk(context: Context, appId: Int, appName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Проверяем разрешение на установку
                if (!hasInstallPermission(context)) {
                    return@withContext Result.failure(Exception("Требуется разрешение на установку из неизвестных источников"))
                }

                // Скачать APK
                val apkFile = downloadApk(context, appId, appName)

                // Установить APK
                installApk(context, apkFile)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun downloadApk(context: Context, appId: Int, appName: String): File {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val url = "$BASE_URL/api/apps/$appId/download"

            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "SigmaStore-Android-App")
                    .addHeader("Accept", "application/vnd.android.package-archive")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Ошибка загрузки APK: ${response.code} - ${response.message}")
                }

                response.body?.let { body ->
                    // Создать директорию для APK в кэше
                    val apkDir = File(context.cacheDir, APK_DIRECTORY)
                    if (!apkDir.exists()) {
                        apkDir.mkdirs()
                    }

                    // Создать файл для APK
                    val fileName = "${appName.replace(" ", "_")}_$appId.apk"
                    val apkFile = File(apkDir, fileName)

                    // Сохранить APK
                    FileOutputStream(apkFile).use { outputStream ->
                        body.byteStream().copyTo(outputStream)
                    }

                    response.close()
                    return@withContext apkFile
                }

                throw Exception("Пустой ответ от сервера")
            } catch (e: Exception) {
                throw Exception("Ошибка при скачивании: ${e.message}")
            }
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        try {
            // Проверяем что файл существует и доступен для чтения
            if (!apkFile.exists() || !apkFile.canRead()) {
                throw Exception("APK файл не найден или недоступен для чтения")
            }

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }

            if (installIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(installIntent)
            } else {
                throw Exception("Не найдено приложение для установки APK")
            }
        } catch (e: Exception) {
            throw Exception("Ошибка установки: ${e.message}")
        }
    }

    // Альтернативный метод установки
    fun installApkWithSystemDialog(context: Context, apkFile: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            throw Exception("Ошибка системной установки: ${e.message}")
        }
    }

    // Проверка доступности endpoint'а
    suspend fun testDownloadEndpoint(appId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$BASE_URL/api/apps/$appId/download")
                    .head()
                    .build()

                val response = client.newCall(request).execute()
                val isAvailable = response.isSuccessful
                response.close()
                isAvailable
            } catch (e: Exception) {
                false
            }
        }
    }
}