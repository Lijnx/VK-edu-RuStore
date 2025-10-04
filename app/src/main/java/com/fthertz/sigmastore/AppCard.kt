package com.fthertz.sigmastore

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource

// --- APP CARD ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppInfo,
    onInstallClick: (String) -> Unit,
    onScreenshotClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Иконка + Название - ТОЛЬКО ServerImage
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServerImage(
                    imageName = app.icon_url ?: "",
                    contentDescription = app.app_name ?: "App Icon",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = R.drawable.placeholder,
                    errorImage = R.drawable.rustore_logo
                )

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        app.app_name ?: "Неизвестное приложение",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        app.developer ?: "Неизвестный разработчик",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Описание
            Text(
                app.description ?: "Описание отсутствует",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Категория + Рейтинг
            Row {
                Text(
                    "Категория: ${app.category ?: "Не указана"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Возраст: ${app.age_rating ?: "Не указано"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Свайпер скриншотов - ТОЛЬКО ServerImage
            if (app.screenshots.isNotEmpty()) {
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { app.screenshots.size })

                HorizontalPager(state = pagerState) { page ->
                    ServerImage(
                        imageName = app.screenshots[page],
                        contentDescription = "Screenshot ${page + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onScreenshotClick(page) },
                        placeholder = R.drawable.placeholder,
                        errorImage = R.drawable.error_image
                    )
                }

                // Индикатор страниц
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(app.screenshots.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (selected) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка установки
            if (app.is_real_apk) {
                Button(
                    onClick = { onInstallClick(app.apk_file) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Установить")
                }
            }
        }
    }
}
// --- DIALOG ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenshotDialog(
    screenshots: List<String>, // Имена файлов скриншотов
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { screenshots.size })

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(state = pagerState) { page ->
                ServerImage(
                    imageName = screenshots[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = R.drawable.placeholder,
                    errorImage = R.drawable.error_image
                )
            }

            // Кнопка закрытия
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Закрыть",
                    tint = Color.White
                )
            }
        }
    }
}
// --- SCREEN ---
@Composable
fun AppCardScreen(app: AppInfo) {
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    Box {
        AppCard(
            app = app,
            onInstallClick = { apkUrl ->
                // DownloadManager для установки
            },
            onScreenshotClick = { index ->
                fullscreenIndex = index
            }
        )

        fullscreenIndex?.let { index ->
            ScreenshotDialog(
                screenshots = app.screenshots,
                startIndex = index,
                onDismiss = { fullscreenIndex = null }
            )
        }
    }
}
