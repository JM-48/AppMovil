package com.example.catalogoproductos.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catalogoproductos.network.OrdenDTO
import com.example.catalogoproductos.viewmodel.AdminOrdenesViewModel
import com.example.catalogoproductos.util.CurrencyUtils
import com.example.catalogoproductos.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdenesScreen(
    navController: NavController,
    viewModel: AdminOrdenesViewModel
) {
    val ordenes by viewModel.ordenes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var ordenSeleccionada by remember { mutableStateOf<OrdenDTO?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            // Mostrar toast o snackbar si fuera necesario, o simplemente limpiar
            kotlinx.coroutines.delay(2000)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Administración de Órdenes",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    items(ordenes) { orden ->
                        AdminOrdenItemCard(
                            orden = orden,
                            onEditClick = {
                                ordenSeleccionada = orden
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDialog && ordenSeleccionada != null) {
            EditarEstadoOrdenDialog(
                orden = ordenSeleccionada!!,
                onDismiss = { showDialog = false },
                onConfirm = { nuevoEstado ->
                    viewModel.actualizarEstadoOrden(ordenSeleccionada!!.id, nuevoEstado)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AdminOrdenItemCard(
    orden: OrdenDTO,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orden #${orden.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Usuario: ${orden.destinatario ?: "Desconocido"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Fecha: ${DateUtils.formatDate(orden.fechaPedido ?: orden.createdAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCLP(orden.total),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusChip(status = orden.status)
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Estado")
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    Text(
                        text = "Detalle de productos:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    orden.items.forEach { item ->
                        DetalleOrdenItem(item)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dirección de envío:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("${orden.direccion}, ${orden.ciudad}, ${orden.region}")
                    Text("CP: ${orden.codigoPostal}")
                }
            }
        }
    }
}

@Composable
fun EditarEstadoOrdenDialog(
    orden: OrdenDTO,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val estados = listOf("PENDING", "PAID", "CONFIRMADA", "SHIPPED", "DELIVERED", "CANCELLED", "REJECTED")
    var estadoSeleccionado by remember { mutableStateOf(orden.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar Estado de Orden #${orden.id}") },
        text = {
            Column {
                estados.forEach { estado ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { estadoSeleccionado = estado }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (estado == estadoSeleccionado),
                            onClick = { estadoSeleccionado = estado }
                        )
                        Text(
                            text = estado,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(estadoSeleccionado) }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
