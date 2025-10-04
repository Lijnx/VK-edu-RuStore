package com.fthertz.sigmastore

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

//@Composable
//fun AppListScreen(parentNavController: NavHostController, apps: List<AppInfo>) {
//    val categories = listOf("Все") + apps.map { it.category }.distinct()
//
//    var selectedTabIndex by remember { mutableStateOf(0) }
//
//    Scaffold { padding ->
//        Column(modifier = Modifier.padding(padding)) {
//            Text(
//                text = "Приложения",
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
//                categories.forEachIndexed { index, category ->
//                    Tab(
//                        selected = selectedTabIndex == index,
//                        onClick = { selectedTabIndex = index },
//                        text = { Text(category) }
//                    )
//                }
//            }
//
//            val filteredApps = if (selectedTabIndex == 0) apps
//            else apps.filter { it.category == categories[selectedTabIndex] }
//
//            LazyColumn {
//                items(filteredApps) { app ->
//                    AppListItem(app = app, onClick = {
//                        parentNavController.navigate("detail/${app.id}")
//                    })
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppListItem(app: AppInfo, onClick: () -> Unit) {
//    Card(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(8.dp)
//        ) {
//            Image(
//                painter = painterResource(app.icon),
//                contentDescription = app.name,
//                modifier = Modifier.size(48.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Column {
//                Text(app.name, fontWeight = FontWeight.Bold)
//                Text(app.description)
//                Text("Категория: ${app.category}", style = MaterialTheme.typography.bodySmall)
//            }
//        }
//    }
//}

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage

@Composable
fun AppListScreen(parentNavController: NavHostController, apps: List<AppInfo>) {
    val categories = listOf("Все") + apps.map { it.category }.distinct()
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
                        text = { Text(category) }
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Загружаем иконку из assets или URL
            AsyncImage(
                model = "file:///android_asset/icons/${app.icon_url}", // пример: "icon.png"
                contentDescription = app.app_name,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(app.app_name, fontWeight = FontWeight.Bold)
                Text(app.description)
                Text("Категория: ${app.category}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
