package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.RegisterViewModel
import androidx.compose.material3.TextFieldDefaults
import com.example.catalogoproductos.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = registerViewModel.nombre,
                onValueChange = { registerViewModel.updateNombre(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.nombreError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.nombreError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Apellido
            OutlinedTextField(
                value = registerViewModel.apellido,
                onValueChange = { registerViewModel.updateApellido(it) },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.apellidoError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.apellidoError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Email
            OutlinedTextField(
                value = registerViewModel.email,
                onValueChange = { registerViewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.emailError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.emailError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Dirección
            OutlinedTextField(
                value = registerViewModel.direccion,
                onValueChange = { registerViewModel.updateDireccion(it) },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.direccionError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.direccionError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Teléfono
            OutlinedTextField(
                value = registerViewModel.telefono,
                onValueChange = { registerViewModel.updateTelefono(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.telefonoError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.telefonoError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Región
            var expandedRegion by remember { mutableStateOf(false) }
            val regiones = remember { registerViewModel.regionesYComunas.map { it.region }.filter { it.isNotBlank() } }
            ExposedDropdownMenuBox(
                expanded = expandedRegion,
                onExpandedChange = { expandedRegion = !expandedRegion }
            ) {
                OutlinedTextField(
                    value = registerViewModel.region,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Región") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) },
                    isError = registerViewModel.regionError != null,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                ExposedDropdownMenu(
                    expanded = expandedRegion,
                    onDismissRequest = { expandedRegion = false }
                ) {
                    regiones.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                registerViewModel.updateRegion(region)
                                expandedRegion = false
                            }
                        )
                    }
                }
            }
            registerViewModel.regionError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Comuna
            var expandedComuna by remember { mutableStateOf(false) }
            val comunas = remember(registerViewModel.region) {
                registerViewModel.regionesYComunas.firstOrNull { it.region == registerViewModel.region }?.comunas?.filter { it.isNotBlank() } ?: emptyList()
            }
            ExposedDropdownMenuBox(
                expanded = expandedComuna,
                onExpandedChange = { if (comunas.isNotEmpty()) expandedComuna = !expandedComuna }
            ) {
                OutlinedTextField(
                    value = registerViewModel.comuna,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Comuna") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedComuna) },
                    isError = registerViewModel.comunaError != null,
                    enabled = comunas.isNotEmpty(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                ExposedDropdownMenu(
                    expanded = expandedComuna,
                    onDismissRequest = { expandedComuna = false }
                ) {
                    comunas.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                registerViewModel.updateComuna(c)
                                expandedComuna = false
                            }
                        )
                    }
                }
            } // Cierre faltante de ExposedDropdownMenuBox
            registerViewModel.comunaError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            if (registerViewModel.region.isNotBlank() && comunas.isEmpty()) {
                Text(
                    text = "No hay comunas disponibles para la región seleccionada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            
            // RUT
            OutlinedTextField(
                value = registerViewModel.rut,
                onValueChange = { registerViewModel.updateRut(it) },
                label = { Text("RUT") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.rutError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.rutError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Contraseña
            OutlinedTextField(
                value = registerViewModel.password,
                onValueChange = { registerViewModel.updatePassword(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.passwordError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
            registerViewModel.passwordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Confirmar Contraseña
            OutlinedTextField(
                value = registerViewModel.confirmPassword,
                onValueChange = { registerViewModel.updateConfirmPassword(it) },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                isError = registerViewModel.confirmPasswordError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
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
            registerViewModel.confirmPasswordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Botón de registro
            GradientButton(
                text = "Registrarse",
                onClick = {
                    val ok = registerViewModel.register()
                    if (ok) {
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            // Enlace para ir a login (eliminado por duplicado y confusión)


            Spacer(modifier = Modifier.height(10.dp))
            
            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                    restoreState = false
                }
            }) {
                Text("¿Ya tienes una cuenta? Inicia sesión", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
