package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inicio de Sesión", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            if (viewModel.login(email, password)) {
                if (viewModel.esAdministrador.value) {
                    navController.navigate("backoffice") {
                        // Limpia el stack de navegación para que no se pueda volver atrás
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.navigate("catalogo") {
                        // Limpia el stack de navegación para que no se pueda volver atrás
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }

        Text(viewModel.mensaje.value, modifier = Modifier.padding(top = 10.dp))
    }
}