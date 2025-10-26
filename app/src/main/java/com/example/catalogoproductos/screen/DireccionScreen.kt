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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catalogoproductos.model.Direccion
import com.example.catalogoproductos.viewmodel.AuthViewModel
import com.example.catalogoproductos.viewmodel.DireccionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionScreen(
    navController: NavController,
    direccionViewModel: DireccionViewModel,
    authViewModel: AuthViewModel
) {
    val currentUserEmail = authViewModel.usuarioActual.value
    val scrollState = rememberScrollState()
    val guardadoExitoso by direccionViewModel.guardadoExitoso.collectAsState()
    
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            navController.navigate("confirmacion")
            direccionViewModel.resetGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dirección de Envío") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
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
            // Calle
            OutlinedTextField(
                value = direccionViewModel.calle,
                onValueChange = { direccionViewModel.updateCalle(it) },
                label = { Text("Calle") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.calleError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            direccionViewModel.calleError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Número
            OutlinedTextField(
                value = direccionViewModel.numero,
                onValueChange = { direccionViewModel.updateNumero(it) },
                label = { Text("Número") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.numeroError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            direccionViewModel.numeroError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Ciudad
            OutlinedTextField(
                value = direccionViewModel.ciudad,
                onValueChange = { direccionViewModel.updateCiudad(it) },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.ciudadError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            direccionViewModel.ciudadError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Provincia
            OutlinedTextField(
                value = direccionViewModel.provincia,
                onValueChange = { direccionViewModel.updateProvincia(it) },
                label = { Text("Provincia") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.provinciaError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            direccionViewModel.provinciaError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Código Postal
            OutlinedTextField(
                value = direccionViewModel.codigoPostal,
                onValueChange = { direccionViewModel.updateCodigoPostal(it) },
                label = { Text("Código Postal") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.codigoPostalError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            direccionViewModel.codigoPostalError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Teléfono
            OutlinedTextField(
                value = direccionViewModel.telefono,
                onValueChange = { direccionViewModel.updateTelefono(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                isError = direccionViewModel.telefonoError != null,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
            direccionViewModel.telefonoError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Checkbox para dirección predeterminada
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = direccionViewModel.esDefault,
                    onCheckedChange = { direccionViewModel.updateEsDefault(it) }
                )
                Text("Establecer como dirección predeterminada")
            }

            // Mensaje de error general
            if (direccionViewModel.errorMessage.isNotEmpty()) {
                Text(
                    text = direccionViewModel.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Botón de guardar
            Button(
                onClick = { 
                    currentUserEmail?.let { email ->
                        direccionViewModel.guardarDireccion(email)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continuar con el pago", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}