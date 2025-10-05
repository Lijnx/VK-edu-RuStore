package com.fthertz.sigmastore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import com.example.myapplication.ui.theme.MyApplicationTheme
//import com.google.ai.client.generativeai.Chat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


data class Message(
    val message: String
)

@Composable
fun SimpleChatScreen() {
    // Автоматически используем адрес для эмулятора

    var messageText by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<Message>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val moshi = remember {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val messageAdapter = remember { moshi.adapter(Message::class.java) }
    val listAdapter = remember {
        moshi.adapter<List<Message>>(
            Types.newParameterizedType(List::class.java, Message::class.java)
        )
    }

    // Функция для получения сообщений
    val fetchMessages = {
        isLoading = true

        val request = Request.Builder()
            .url("$ML_URL/messages")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                errorMessage = "Ошибка подключения: ${e.message}"
                isLoading = false
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                response.close()

                if (response.isSuccessful && body != null) {
                    try {
                        val messages = listAdapter.fromJson(body)
                        if (messages != null) {
                            chatMessages = messages
                            errorMessage = null
                        }
                    } catch (e: Exception) {
                        errorMessage = "Ошибка загрузки сообщений"
                    }
                } else {
                    errorMessage = "Ошибка сервера"
                }
                isLoading = false
            }
        })
    }

    // Функция для отправки сообщения
    val sendMessage = {
        if (messageText.isNotBlank() && !isLoading) {
            isLoading = true
            val message = Message(messageText)

            try {
                val json = messageAdapter.toJson(message)
                val body = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$ML_URL/chat")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        errorMessage = "Ошибка отправки"
                        isLoading = false
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.close()
                        if (response.isSuccessful) {
                            messageText = ""
                            // После отправки обновляем список сообщений
                            fetchMessages()
                        } else {
                            errorMessage = "Ошибка отправки"
                            isLoading = false
                        }
                    }
                })
            } catch (e: Exception) {
                errorMessage = "Ошибка"
                isLoading = false
            }
        }
    }


    val clearMessageList = {
        isLoading = true

        // Создаем пустое тело запроса для DELETE
        val body = "".toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$ML_URL/messages")
            .delete(body)  // DELETE с пустым телом
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                errorMessage = "Ошибка очистки: ${e.message}"
                isLoading = false
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()
                if (response.isSuccessful) {
                    // Очищаем локальный список
                    chatMessages = emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Ошибка очистки: ${response.code}"
                }
                isLoading = false
            }
        })
    }

// Автоматически загружаем сообщения при запуске
    LaunchedEffect(Unit) {
        fetchMessages()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Шапка чата
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Helpy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Область сообщений
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Загрузка...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            "Начните диалог с Helpy",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Опиши приложение, которое ты хочешь найти, и я постараюсь помочь",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(chatMessages.reversed()) { message ->
                        // Сообщение пользователя (справа)
                        if (/* условие для определения пользователя */ true) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                                ) {
                                    Text(
                                        text = message.message,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        // Сообщение ассистента (слева)
                        else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                                ) {
                                    Text(
                                        text = message.message,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Панель ввода (фиксированная внизу)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Показываем ошибки
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Поле ввода сообщения
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Введите сообщение...") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    enabled = !isLoading,
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (messageText.isNotBlank()) {
                            IconButton(
                                onClick = { messageText = "" },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Кнопка отправки
                IconButton(
                    onClick = sendMessage,
                    enabled = messageText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (messageText.isNotBlank() && !isLoading)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Отправить",
                            modifier = Modifier.size(20.dp),
                            tint = if (messageText.isNotBlank() && !isLoading)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Кнопка нового чата
            TextButton(
                onClick = clearMessageList,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Новый чат",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Новый чат",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}