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
    onScreenshotClick: (Int) -> Unit // теперь передаём индекс
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Иконка + Название
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = app.icon_url,
                    contentDescription = app.app_name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.rustore_logo)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(app.app_name, style = MaterialTheme.typography.titleLarge)
                    Text(app.developer, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Описание
            Text(app.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Категория + Рейтинг
            Row {
                Text("Категория: ${app.category}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Возраст: ${app.age_rating}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Свайпер скриншотов
            if (app.screenshots.isNotEmpty()) {
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { app.screenshots.size })

                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = "file:///android_asset/${app.screenshots[page]}",
                        contentDescription = "Screenshot ${page + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onScreenshotClick(page) },
                        contentScale = ContentScale.Crop
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
    screenshots: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { screenshots.size })

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(state = pagerState) { page ->
                val bitmap = remember(screenshots[page]) {
                    try {
                        val inputStream = context.assets.open(screenshots[page])
                        BitmapFactory.decodeStream(inputStream)
                    } catch (e: Exception) {
                        null
                    }
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
