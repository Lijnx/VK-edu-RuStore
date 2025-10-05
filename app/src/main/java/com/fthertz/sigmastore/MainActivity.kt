package com.fthertz.sigmastore

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.fthertz.sigmastore.ui.theme.ComposeAppTheme

const val BASE_URL = "http://10.9.76.78:8000"
const val ML_URL = "http://10.116.64.20:8000"
// -------- SCREENS --------

@Composable
fun OnboardingScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.rustore_logo),
                contentDescription = "RuStore Logo"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Добро пожаловать в RuStore!", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("signup") }) {
                Text("Создать аккаунт")
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    object AppList : Screen("app_list", "Приложения", R.drawable.applist_logo)
    object NeuralNetwork : Screen("neural_network", "Нейросеть", R.drawable.neural_logo)
    object Search : Screen("search", "Поиск", R.drawable.search_logo)
}

// -------- NAVIGATION --------
@Composable
fun RuStoreApp(userRepository: UserRepository, apps: List<AppInfo>) {
    val navController = rememberNavController()
    val startDestination = if (userRepository.isRegistered()) "main" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDestination

    ) {
        // Экран онбординга
        composable("onboarding") {
            OnboardingScreen(navController)
        }

        // Экран регистрации
        composable("signup") {
            SignUpScreen(navController, userRepository)
        }

        // Основная часть с нижней навигацией
        composable("main") {
            MainScreen(navController, apps)
        }

        // Детальная карточка приложения (открывается поверх main)
        composable("detail/{appId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("appId")?.toIntOrNull() ?: -1
            val app = apps.firstOrNull { it.id == id }
            if (app != null) {
                AppCardScreen(
                    app = app,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Приложение не найдено")
                }
            }
        }
    }
}


@Composable
fun MainScreen(parentNavController : NavHostController, apps: List<AppInfo>) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.AppList,
        Screen.NeuralNetwork,
        Screen.Search
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(screen.label) },
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.iconRes),
                                contentDescription = screen.label
                            )
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AppList.route,
            modifier = Modifier
                .fillMaxSize() //???
                .padding(innerPadding)
        ) {
            composable(Screen.AppList.route) { AppListScreen(parentNavController, apps) }
            composable(Screen.NeuralNetwork.route) { SimpleChatScreen() }
            composable(Screen.Search.route) { AppSearchScreen(parentNavController, apps) }
        }
    }
}

// -------- MAIN --------
class MainActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    // Для обработки результата запроса разрешения
    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Проверяем разрешение после возврата из настроек
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                // Разрешение получено
                Toast.makeText(this, "Разрешение на установку получено", Toast.LENGTH_SHORT).show()
            } else {
                // Разрешение не получено
                Toast.makeText(this, "Для установки приложений необходимо разрешение", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(this)

        // Проверить разрешение при запуске (без автоматического запроса)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()) {
            // Просто логируем, но не запрашиваем автоматически
            println("Требуется разрешение на установку из неизвестных источников")
        }

        val apps = AppRepository.loadAppsFromAssets(this)

        setContent {
            ComposeAppTheme {
                RuStoreApp(userRepository, apps)
            }
        }
    }

    // Функция для запроса разрешения (можно вызвать из UI)
    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                installPermissionLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Не удалось открыть настройки разрешений", Toast.LENGTH_SHORT).show()
            }
        }
    }
}