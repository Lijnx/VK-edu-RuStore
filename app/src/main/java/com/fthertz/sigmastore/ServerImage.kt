// ImageLoader.kt
package com.fthertz.sigmastore

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun ServerImage(
    imageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    errorImage: Int? = null
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imageName) {
        coroutineScope.launch {
            isLoading = true
            error = false
            bitmap = AppRepository.getImage(imageName)
            isLoading = bitmap == null
            error = bitmap == null
        }
    }

    when {
        isLoading && placeholder != null -> {
            // Показываем плейсхолдер
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(placeholder),
                contentDescription = "Загрузка...",
                modifier = modifier
            )
        }
        bitmap != null -> {
            // Показываем загруженное изображение
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        error && errorImage != null -> {
            // Показываем изображение ошибки
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(errorImage),
                contentDescription = "Ошибка загрузки",
                modifier = modifier
            )
        }
        else -> {
            // Пустой элемент если нет плейсхолдера/ошибки
            androidx.compose.foundation.layout.Box(modifier = modifier)
        }
    }
}