package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.AuthViewModel
import com.example.catalogoproductos.viewmodel.CatalogoViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CatalogoScreen(
    navController: NavController,
    catalogoViewModel: CatalogoViewModel,
    authViewModel: AuthViewModel
) {
    val productos by catalogoViewModel.productos.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        catalogoViewModel.cargarProductos(context)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de login exitoso cuando se ingresa al catálogo
    LaunchedEffect(authViewModel.mensaje.value, authViewModel.esAdministrador.value) {
        val msg = authViewModel.mensaje.value
        if (msg.isNotBlank() && !authViewModel.esAdministrador.value) {
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            // Limpiar el mensaje para evitar repeticiones
            authViewModel.mensaje.value = ""
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
                .padding(16.dp)
        ) {
            Text("Catálogo de Productos", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(12.dp))

            if (productos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productos) { producto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Imagen grande con precio arriba a la derecha
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(producto.imagen ?: ""),
                                        contentDescription = producto.nombre,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(producto.precio),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Texto abajo: nombre y descripción
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val descripcion = producto.descripcion ?: ""
                                if (descripcion.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = descripcion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Botón abajo a la derecha
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = {
                                        navController.navigate("detalle/${producto.id}")
                                    }) {
                                        Text("Ver detalle")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}