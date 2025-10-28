package com.example.catalogoproductos.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.catalogoproductos.components.GradientButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.PerfilUsuarioViewModel
import com.example.catalogoproductos.viewmodel.AuthViewModel
import com.example.catalogoproductos.repository.RegionComunaRepository
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import com.example.catalogoproductos.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    navController: NavController,
    perfilViewModel: PerfilUsuarioViewModel,
    authViewModel: AuthViewModel
) {
    val scrollState = rememberScrollState()
    val guardadoExitoso by perfilViewModel.guardadoExitoso.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val popped = navController.popBackStack()
                        if (!popped) {
                            navController.navigate("catalogo") {
                                popUpTo("catalogo") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Sin acciones adicionales para simplificar la UI
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                text = data.visuals.actionLabel ?: "",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Text(data.visuals.message, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título de la sección
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Email (no editable)
                OutlinedTextField(
                    value = perfilViewModel.email,
                    onValueChange = { },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White
                    ),
                    enabled = false,
                    singleLine = true
                )

                // Nombre
                OutlinedTextField(
                    value = perfilViewModel.nombre,
                    onValueChange = { perfilViewModel.updateNombre(it) },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = perfilViewModel.nombreError != null,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Nombre")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                perfilViewModel.nombreError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Apellido
                OutlinedTextField(
                    value = perfilViewModel.apellido,
                    onValueChange = { perfilViewModel.updateApellido(it) },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = perfilViewModel.apellidoError != null,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Apellido")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                perfilViewModel.apellidoError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Teléfono
                OutlinedTextField(
                    value = perfilViewModel.telefono,
                    onValueChange = { perfilViewModel.updateTelefono(it) },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = perfilViewModel.telefonoError != null,
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Teléfono")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                perfilViewModel.telefonoError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Dirección
                OutlinedTextField(
                    value = perfilViewModel.direccion,
                    onValueChange = { perfilViewModel.updateDireccion(it) },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = perfilViewModel.direccionError != null,
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = "Dirección")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                perfilViewModel.direccionError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Región y Comuna
                val context = LocalContext.current
                val regionesYComunas = remember { RegionComunaRepository().cargarDesdeAssets(context) }
                var selectedRegion by remember { mutableStateOf("") }
                var expandedRegion by remember { mutableStateOf(false) }
                var expandedComuna by remember { mutableStateOf(false) }

                // Inicializar la región seleccionada según la comuna ya guardada
                LaunchedEffect(regionesYComunas, perfilViewModel.ciudad) {
                    val found = regionesYComunas.firstOrNull { it.comunas.contains(perfilViewModel.ciudad) }
                    selectedRegion = found?.region ?: selectedRegion
                }

                val regiones = remember(regionesYComunas) {
                    regionesYComunas.map { it.region }.filter { it.isNotBlank() }
                }
                val comunas = remember(selectedRegion) {
                    regionesYComunas.firstOrNull { it.region == selectedRegion }?.comunas?.filter { it.isNotBlank() } ?: emptyList()
                }

                // Región
                ExposedDropdownMenuBox(
                    expanded = expandedRegion,
                    onExpandedChange = { expandedRegion = !expandedRegion }
                ) {
                    OutlinedTextField(
                        value = selectedRegion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Región") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRegion,
                        onDismissRequest = { expandedRegion = false }
                    ) {
                        regiones.forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region) },
                                onClick = {
                                    selectedRegion = region
                                    // Resetear la comuna cuando cambia la región
                                    perfilViewModel.updateCiudad("")
                                    expandedRegion = false
                                }
                            )
                        }
                    }
                }

                // Comuna
                ExposedDropdownMenuBox(
                    expanded = expandedComuna,
                    onExpandedChange = { if (comunas.isNotEmpty()) expandedComuna = !expandedComuna }
                ) {
                    OutlinedTextField(
                        value = perfilViewModel.ciudad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedComuna) },
                        isError = perfilViewModel.ciudadError != null,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                        ),
                        enabled = comunas.isNotEmpty(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedComuna,
                        onDismissRequest = { expandedComuna = false }
                    ) {
                        comunas.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    perfilViewModel.updateCiudad(c)
                                    expandedComuna = false
                                }
                            )
                        }
                    }
                }
                perfilViewModel.ciudadError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Código Postal
                OutlinedTextField(
                    value = perfilViewModel.codigoPostal,
                    onValueChange = { perfilViewModel.updateCodigoPostal(it) },
                    label = { Text("Código Postal") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = perfilViewModel.codigoPostalError != null,
                    leadingIcon = {
                        Icon(Icons.Default.Pin, contentDescription = "Código Postal")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
                perfilViewModel.codigoPostalError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Mensaje de error general
                if (perfilViewModel.errorMessage.isNotEmpty()) {
                    Text(
                        text = perfilViewModel.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Mensaje de éxito
                if (guardadoExitoso) {
                    Text(
                        text = "Perfil actualizado correctamente",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Botón de guardar
                GradientButton(
                    text = "Guardar Cambios",
                    onClick = { perfilViewModel.guardarPerfil() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                // Botón de cerrar sesión
                GradientButton(
                    text = "Cerrar sesión",
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                        val activity = (context as? Activity)
                        activity?.let {
                            it.finishAffinity()
                            it.startActivity(Intent(it, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
