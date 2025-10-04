package com.fthertz.sigmastore.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

// 1. Определяем цвета для светлой темы
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0077FF),
    onPrimary = Color.White,
    secondary = Color(0xFFF2F3F5),
    onSecondary = Color(color = 0xFF6D7885),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(color = 0xFFF7F7F7),
    onSurface = Color(0xFF6D7885),
)

// 2. Определяем цвета для тёмной темы
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0077FF),
    onPrimary = Color(0xFFEBF1F6),
    secondary = Color(0xFF03DAC5),
    onSecondary = Color(0xFF96A6B1),
    background = Color(0xFF1A1C20),
    onBackground = Color(0xFFEBF1F6),
    surface = Color(0xFF282B30),
    onSurface = Color(0xFF9BA6B1),
)


@Composable
fun ComposeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}