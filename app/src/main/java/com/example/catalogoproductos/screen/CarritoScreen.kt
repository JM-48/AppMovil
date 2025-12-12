package com.example.catalogoproductos.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.viewmodel.CarritoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.catalogoproductos.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
    carritoViewModel: CarritoViewModel
) {
    val itemsCarrito by carritoViewModel.itemsCarrito.collectAsState()
    val totalCarrito by carritoViewModel.totalCarrito.collectAsState()
    val cantidadItems by carritoViewModel.cantidadItems.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var stockMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    LaunchedEffect(Unit) {
        try {
            val repo = com.example.catalogoproductos.repository.ProductoRepository()
            val productos = repo.obtenerProductosDesdeApi()
            stockMap = productos.associate { it.id to it.stock }
        } catch (_: Exception) {
            stockMap = emptyMap()
        }
    }
    
    // Estados para animaciones y feedback
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var actionInProgress by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                text = data.visuals.actionLabel ?: "",
                                color = Color.White
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
                modifier = Modifier.fillMaxSize()
            ) {
                if (itemsCarrito.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Carrito vacío",
                                modifier = Modifier.size(100.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Tu carrito está vacío",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GradientButton(
                                text = "Ir a comprar",
                                onClick = { navController.navigate("catalogo") },
                                modifier = Modifier.width(200.dp).height(50.dp),
                                icon = Icons.Default.ShoppingBasket
                            )
                        }
                    }
                } else if (!isLoading) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(itemsCarrito) { item ->
                            ItemCarritoCard(
                                item = item,
                                onIncrement = { 
                                    scope.launch {
                                        val max = stockMap[item.productoId] ?: Int.MAX_VALUE
                                        if (item.cantidad + 1 > max) {
                                            snackbarHostState.showSnackbar(
                                                message = "No puedes comprar más que el stock disponible",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            carritoViewModel.actualizarCantidad(item.id, item.cantidad + 1)
                                        }
                                    }
                                },
                                onDecrement = { 
                                    if (item.cantidad > 1) {
                                        scope.launch {
                                            carritoViewModel.actualizarCantidad(item.id, item.cantidad - 1)
                                        }
                                    }
                                },
                                onDelete = { 
                                    scope.launch {
                                        carritoViewModel.eliminarItem(item.id)
                                        snackbarHostState.showSnackbar(
                                                message = "Producto eliminado del carrito",
                                                duration = SnackbarDuration.Short
                                            )
                                    }
                                }
                            )
                        }
                    }

                    // Resumen del carrito
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cantidad de productos:", fontWeight = FontWeight.Medium)
                                Text("$cantidadItems", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    text = totalCarrito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            GradientButton(
                                text = "Proceder al pago",
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        delay(800)
                                        val exceso = itemsCarrito.any { it.cantidad > (stockMap[it.productoId] ?: Int.MAX_VALUE) }
                                        val sinStock = itemsCarrito.any { (stockMap[it.productoId] ?: 0) == 0 }
                                        isLoading = false
                                        if (exceso) {
                                            snackbarHostState.showSnackbar(
                                                message = "Tu carrito excede el stock disponible",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else if (sinStock) {
                                            snackbarHostState.showSnackbar(
                                                message = "Hay productos sin stock en tu carrito",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            navController.navigate("direccion")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = !isLoading && !actionInProgress && itemsCarrito.all { (stockMap[it.productoId] ?: 0) > 0 },
                                icon = Icons.Default.Payment
                            )
                        }
                    }
                }
            }
            
            // Indicador de carga
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Procesando...", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            // Mensaje de éxito
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Éxito",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Carrito vaciado con éxito",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCarritoCard(
    item: ItemCarrito,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Image(
                painter = rememberAsyncImagePainter(item.imagen),
                contentDescription = item.nombre,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
            
            // Información del producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(item.precio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subtotal: " + com.example.catalogoproductos.util.CurrencyUtils.formatCLP(item.precio * item.cantidad),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Controles de cantidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onDecrement) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Disminuir cantidad",
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Text(
                    text = "${item.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onIncrement) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aumentar cantidad",
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar producto",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
