package com.fthertz.sigmastore

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

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
                    AppSuggestionCard(
                        app = app,
                        onClick = {
                            parentNavController.navigate("detail/${app.id}")
                        }
                    )
                }
            }
        }
    }
}
