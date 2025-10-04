package com.fthertz.sigmastore

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@Composable
fun AppListScreen(parentNavController: NavHostController, apps: List<AppInfo>) {
    // Добавьте проверку на пустой список
    if (apps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет доступных приложений")
        }
        return
    }

    val categories = listOf("Все") + apps.mapNotNull { it.category }.distinct()
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Приложения",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(category ?: "Все")
                        }
                    )
                }
            }

            val filteredApps = if (selectedTabIndex == 0) apps
            else apps.filter { it.category == categories[selectedTabIndex] }

            LazyColumn {
                items(filteredApps) { app ->
                    AppListItem(app = app, onClick = {
                        parentNavController.navigate("detail/${app.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Для локальных данных используем painterResource, для серверных - AsyncImage
            if (app.icon_url?.startsWith("http") == true) {
                AsyncImage(
                    model = app.icon_url,
                    contentDescription = app.app_name ?: "App Icon",
                    modifier = Modifier.size(48.dp),
                    placeholder = painterResource(id = R.drawable.placeholder),
                    error = painterResource(id = R.drawable.rustore_logo)
                )
            } else {
                // Для локальных иконок из ресурсов
                Image(
                    painter = painterResource(id = R.drawable.rustore_logo), // fallback
                    contentDescription = app.app_name ?: "App Icon",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    app.app_name ?: "Неизвестное приложение",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    app.description ?: "Описание отсутствует"
                )
                Text(
                    "Категория: ${app.category ?: "Не указана"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}