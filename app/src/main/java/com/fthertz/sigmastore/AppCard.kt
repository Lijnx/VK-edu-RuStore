package com.fthertz.sigmastore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

// --- APP CARD ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppInfo,
    onInstallClick: (String) -> Unit,
    onScreenshotClick: (Int) -> Unit,
    onBackClick: () -> Unit // Добавляем callback для кнопки назад
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Кнопка назад
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Назад",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Основная карточка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Заголовок с иконкой
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Иконка с градиентной рамкой
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        clip = false
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    ServerImage(
                                        imageName = app.icon_url ?: "",
                                        contentDescription = app.app_name ?: "App Icon",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        placeholder = R.drawable.placeholder,
                                        errorImage = R.drawable.rustore_logo
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Название и разработчик
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = app.app_name ?: "Неизвестное приложение",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.developer ?: "Неизвестный разработчик",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Мета-информация
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Категория
                            Column {
                                Text(
                                    "Категория",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    app.category ?: "Не указана",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Возрастной рейтинг
                            Column {
                                Text(
                                    "Возрастной рейтинг",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    app.age_rating ?: "Не указано",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Описание
                        Text(
                            "Описание",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.description ?: "Описание отсутствует",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.1
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Свайпер скриншотов
                        if (app.screenshots.isNotEmpty()) {
                            Text(
                                "Скриншоты",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val pagerState = rememberPagerState(
                                initialPage = 0,
                                pageCount = { app.screenshots.size }
                            )

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp) // Увеличиваем высоту скриншотов
                            ) { page ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    ServerImage(
                                        imageName = app.screenshots[page],
                                        contentDescription = "Screenshot ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onScreenshotClick(page) },
                                        placeholder = R.drawable.placeholder,
                                        errorImage = R.drawable.error_image
                                    )
                                }
                            }

                            // Индикатор страниц
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(app.screenshots.size) { index ->
                                    val selected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 12.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Кнопка установки
                        if (app.is_real_apk) {
                            Button(
                                onClick = { onInstallClick(app.apk_file) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    "Установить",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
//        // Добавляем нижний отступ для системной навигации
//        Spacer(modifier = Modifier.height(16.dp))
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
fun AppCardScreen(app: AppInfo, onBackClick: () -> Unit) { // Добавляем параметр onBackClick
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    Box {
        AppCard(
            app = app,
            onInstallClick = { apkUrl ->
                // DownloadManager для установки
            },
            onScreenshotClick = { index ->
                fullscreenIndex = index
            },
            onBackClick = onBackClick // Передаем callback
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
