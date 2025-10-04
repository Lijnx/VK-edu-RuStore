package com.fthertz.sigmastore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
            MainScreen(navController, apps) // <-- внутри MainScreen есть свой NavHost
        }

        // Детальная карточка приложения (открывается поверх main)
        composable("detail/{appId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("appId")?.toIntOrNull() ?: -1
            val app = apps.firstOrNull { it.id == id }
            if (app != null) {
                AppCardScreen(app)
            } else {
                // Показываем экран ошибки если приложение не найдено
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
        contentWindowInsets = WindowInsets.systemBars // учитываем статусбар и навигацию
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
            composable(Screen.Search.route) { SearchScreen() }
        }
    }
}


@Composable
fun NeuralNetworkScreen() {

}

@Composable
fun SearchScreen() {

}

// -------- MAIN --------
class MainActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(this)

        // Загружаем данные синхронно из assets (без корутин)
        val apps = AppRepository.loadAppsFromAssets(this)

        setContent {
            ComposeAppTheme {
                RuStoreApp(userRepository, apps)
            }
        }
    }
}
