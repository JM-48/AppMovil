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

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes como pop-up (Snackbar) cuando cambie el mensaje del ViewModel
    LaunchedEffect(viewModel.mensaje.value) {
        val msg = viewModel.mensaje.value
        if (msg.isNotBlank()) {
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    action = {
                        data.visuals.actionLabel?.let { label ->
                            TextButton(onClick = { data.performAction() }) { Text(label) }
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        }
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (emailError == null) {
                        if (viewModel.login(email, password)) {
                            if (viewModel.esAdministrador.value) {
                                navController.navigate("backoffice") {
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
                },
                enabled = emailError == null && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            TextButton(onClick = {
                navController.navigate("register") {
                    launchSingleTop = true
                }
            }) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}