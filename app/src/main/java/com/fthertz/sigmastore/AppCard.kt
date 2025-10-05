package com.fthertz.sigmastore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

// --- APP CARD ---
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    app: AppInfo,
    onInstallClick: (String) -> Unit,
    onScreenshotClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var installError by remember { mutableStateOf<String?>(null) }

    // Запускаем эффект скачивания когда isDownloading становится true
    if (isDownloading) {
        DownloadEffect(
            app = app,
            context = context,
            onProgressUpdate = { progress -> downloadProgress = progress },
            onDownloadComplete = {
                isDownloading = false
                downloadProgress = 0f
            },
            onError = { error ->
                installError = error
                isDownloading = false
                downloadProgress = 0f
            }
        )
    }

    // Диалог ошибки
    if (installError != null) {
        AlertDialog(
            onDismissRequest = { installError = null },
            title = { Text("Ошибка установки") },
            text = { Text(installError!!) },
            confirmButton = {
                Button(onClick = { installError = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = app.app_name ?: "Приложение",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // Couldn't find meta-data for provider with authority com.fthertz.sigmastore.provider
        floatingActionButton = {
            if (app.is_real_apk) {
                // Упрощенная кнопка установки
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!isDownloading) {
                            isDownloading = true
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${(downloadProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.GetApp,
                            contentDescription = "Скачать",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Установить",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Основная карточка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            .fillMaxWidth()
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
                                        imagePath = app.icon_url ?: "",
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

                            // Контейнер для скриншотов с фиксированной высотой
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    ServerImage(
                                        imagePath = app.screenshots[page],
                                        contentDescription = "Screenshot ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onScreenshotClick(page) },
                                        placeholder = R.drawable.placeholder,
                                        errorImage = R.drawable.error_image
                                    )
                                }

                                // Индикатор страниц поверх скриншотов
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        repeat(app.screenshots.size) { index ->
                                            val selected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .size(if (selected) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (selected) MaterialTheme.colorScheme.primary
                                                        else Color.White.copy(alpha = 0.7f)
                                                    )
                                            )
                                            if (index < app.screenshots.size - 1) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Добавляем отступ внизу для FAB
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// Отдельная композабл-функция для обработки скачивания
@Composable
fun DownloadEffect(
    app: AppInfo,
    context: android.content.Context,
    onProgressUpdate: (Float) -> Unit,
    onDownloadComplete: () -> Unit,
    onError: (String) -> Unit
) {
    LaunchedEffect(key1 = app.id) {
        try {
            // Симуляция прогресса скачивания
            for (i in 0..100 step 2) {
                onProgressUpdate(i / 100f)
                delay(50L)
            }

            // Реальное скачивание и установка
            // В функции DownloadEffect замените блок установки на:
            val result = try {
                ApkInstaller.downloadAndInstallApk(
                    context = context,
                    appId = app.id,
                    appName = app.app_name ?: "app"
                )
            } catch (e: Exception) {
                // Если основной метод не работает, пробуем альтернативный
                try {
                    val apkFile = ApkInstaller.downloadApk(context, app.id, app.app_name ?: "app")
                    ApkInstaller.installApkWithSystemDialog(context, apkFile)
                    Result.success(Unit)
                } catch (e2: Exception) {
                    Result.failure(e2)
                }
            }

            if (result.isFailure) {
                onError(result.exceptionOrNull()?.message ?: "Неизвестная ошибка установки")
            } else {
                onDownloadComplete()
            }
        } catch (e: Exception) {
            onError(e.message ?: "Ошибка при скачивании")
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
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { screenshots.size })

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(state = pagerState) { page ->
                ServerImage(
                    imagePath = screenshots[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = R.drawable.placeholder,
                    errorImage = R.drawable.error_image
                )
            }

            // Индикатор страниц
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${screenshots.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
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
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- SCREEN ---
@Composable
fun AppCardScreen(app: AppInfo, onBackClick: () -> Unit) {
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    Box {
        AppCard(
            app = app,
            onInstallClick = { apkUrl ->
                // Резервный метод установки
            },
            onScreenshotClick = { index ->
                fullscreenIndex = index
            },
            onBackClick = onBackClick
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