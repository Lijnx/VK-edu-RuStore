package com.fthertz.sigmastore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


@Composable
fun SignUpScreen(navController: NavHostController, userRepository: UserRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Создать аккаунт", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (email.isNotBlank() && password.isNotBlank()) {
                // сохраняем данные через SharedPreferences
                userRepository.registerUser(email, password)

                // переход в магазин
                navController.navigate("main") {
                    popUpTo("signup") { inclusive = true } // убираем экран регистрации из backstack
                }
            }
        }) {
            Text("Зарегистрироваться")
        }
    }
}


class UserRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun isRegistered(): Boolean {
        return prefs.getBoolean("is_registered", false)
    }

    fun registerUser(email: String, password: String) {
        prefs.edit {
            putBoolean("is_registered", true)
                .putString("email", email)
                // пароль лучше не хранить открытым! Для MVP можно, но в реале — хэшировать
                .putString("password", password)
        }
    }

    fun logout() {
        prefs.edit {
            putBoolean("is_registered", false)
                .remove("email")
                .remove("password")
        }
    }

    fun getEmail(): String? = prefs.getString("email", null)
}
