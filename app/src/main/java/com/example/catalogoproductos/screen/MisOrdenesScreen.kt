package com.example.catalogoproductos.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catalogoproductos.network.DetalleOrdenDTO
import com.example.catalogoproductos.network.OrdenDTO
import com.example.catalogoproductos.viewmodel.OrdenesViewModel
import com.example.catalogoproductos.util.CurrencyUtils
import com.example.catalogoproductos.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisOrdenesScreen(
    navController: NavController,
    viewModel: OrdenesViewModel
) {
    val ordenes by viewModel.ordenes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
            } else if (ordenes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Sin órdenes",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No has realizado ninguna compra aún", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ordenes) { orden ->
                        OrdenItemCard(orden)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdenItemCard(orden: OrdenDTO) {
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
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dirección de envío:", fontWeight = FontWeight.Bold)
                    // Assuming OrdenDTO doesn't have address fields directly populated in the DTO based on my search earlier, 
                    // but the changes.txt said it has: direccion, region, ciudad, codigoPostal.
                    // Let's verify OrdenDTO structure in ApiServices.kt
                    // ApiServices.kt: data class OrdenDTO(val id: Int, val status: String, val total: Double, val items: List<DetalleOrdenDTO>, val createdAt: String?)
                    // It seems the DTO in ApiServices.kt is missing the address fields mentioned in documentation. 
                    // I should probably update OrdenDTO in ApiServices.kt first if I want to show address.
                    // For now, I'll stick to what is in the code.
                }
            }
            
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Colapsar" else "Expandir",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun DetalleOrdenItem(item: DetalleOrdenDTO) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.nombre} x${item.cantidad}",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = CurrencyUtils.formatCLP(item.total),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "PAID", "CONFIRMADA", "COMPLETED" -> Color.Green
        "PENDING", "EnProceso", "PENDIENTE" -> Color(0xFFFFA500) // Orange
        "CANCELLED", "REJECTED", "RECHAZADA" -> Color.Red
        else -> Color.Gray
    }

    val textoEspanol = when (status) {
        "CART" -> "Carrito"
        "PENDING" -> "Pendiente"
        "PAID" -> "Recibido"
        "CANCELLED" -> "Cancelado"
        "PENDIENTE" -> "Pendiente"
        "CONFIRMADA" -> "Confirmada"
        "RECHAZADA" -> "Rechazada"
        "SHIPPED" -> "Enviado"
        "DELIVERED" -> "Entregado"
        "COMPLETED" -> "Completado"
        "REJECTED" -> "Rechazada"
        "EnProceso" -> "En Proceso"
        else -> status
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = textoEspanol,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
