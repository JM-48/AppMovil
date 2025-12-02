package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.AuthViewModel
import java.util.regex.Pattern
import androidx.compose.ui.graphics.Color
import com.example.catalogoproductos.components.GradientButton
import androidx.compose.material3.TextFieldDefaults
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Mostrar mensajes como pop-up (Snackbar) cuando cambie el mensaje del ViewModel
    LaunchedEffect(viewModel.mensaje.value) {
        val msg = viewModel.mensaje.value
        if (msg.isNotBlank()) {
            val cleanMsg = msg.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
            Toast.makeText(context, cleanMsg, Toast.LENGTH_SHORT).show()
            // Limpiar mensaje para no repetirlo en recomposiciones
            viewModel.mensaje.value = ""
        }
    }

    fun validateEmail(value: String) {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        emailError = when {
            value.isEmpty() -> "El email es obligatorio"
            !emailPattern.matcher(value).matches() -> "El email no es válido"
            else -> null
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Inicio de Sesión", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    validateEmail(it)
                },
                label = { Text("Email") },
                isError = emailError != null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
            emailError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

            Spacer(modifier = Modifier.height(10.dp))

            GradientButton(
                text = "Entrar",
                onClick = {
                    if (emailError == null) {
                        viewModel.login(email, password)
                    }
                },
                enabled = emailError == null && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            TextButton(onClick = {
                navController.navigate("register") {
                    launchSingleTop = true
                }
            }) {
                Text("¿No tienes cuenta? Regístrate", color = Color.White)
            }
        }
    }
    LaunchedEffect(viewModel.usuarioActual.value, viewModel.esAdministrador.value) {
        val user = viewModel.usuarioActual.value
        if (user != null) {
            if (viewModel.esAdministrador.value) {
                navController.navigate("backoffice_productos") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                    restoreState = false
                }
            } else {
                navController.navigate("catalogo") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        }
    }
}
