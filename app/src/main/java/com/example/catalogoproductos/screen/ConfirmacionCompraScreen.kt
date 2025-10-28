package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catalogoproductos.viewmodel.CarritoViewModel
import com.example.catalogoproductos.viewmodel.DireccionViewModel
import com.example.catalogoproductos.viewmodel.PerfilUsuarioViewModel
import com.example.catalogoproductos.model.PerfilUsuario

import androidx.compose.material3.*
import com.example.catalogoproductos.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionCompraScreen(
    navController: NavController,
    carritoViewModel: CarritoViewModel,
    direccionViewModel: DireccionViewModel,
    email: String,
    perfilUsuarioViewModel: PerfilUsuarioViewModel
) {
    val scrollState = rememberScrollState()
    val itemsCarrito by carritoViewModel.itemsCarrito.collectAsState(initial = emptyList())
    val totalCarrito by carritoViewModel.totalCarrito.collectAsState(initial = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(0))
    val direccionDefault by direccionViewModel.direccionDefault.collectAsState(initial = null)

    LaunchedEffect(email) {
        direccionViewModel.cargarDireccionDefault(email)
    }
    
    var estadoCompra by remember { mutableStateOf<EstadoCompra>(EstadoCompra.EnProceso) }
    var intentosConfirmacion by remember { mutableStateOf(0) }
    
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.White
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
            when (estadoCompra) {
                EstadoCompra.EnProceso -> {
                    val perfilFallback = PerfilUsuario(
                        email = perfilUsuarioViewModel.email,
                        nombre = perfilUsuarioViewModel.nombre,
                        apellido = perfilUsuarioViewModel.apellido,
                        telefono = perfilUsuarioViewModel.telefono,
                        direccion = perfilUsuarioViewModel.direccion,
                        ciudad = perfilUsuarioViewModel.ciudad,
                        codigoPostal = perfilUsuarioViewModel.codigoPostal
                    ).let { perfil ->
                        // Si los campos clave están vacíos, considera que no hay perfil válido
                        if (perfil.direccion.isBlank() && perfil.ciudad.isBlank() && perfil.telefono.isBlank()) null else perfil
                    }

                    ResumenCompraContent(
                        itemsCarrito = itemsCarrito,
                        totalCarrito = totalCarrito,
                        direccion = direccionDefault,
                        perfilFallback = perfilFallback,
                        onCompraClick = {
                            // Lógica determinista: primera confirmación falla, segunda y siguientes exitosas
                            if (intentosConfirmacion == 0) {
                                estadoCompra = EstadoCompra.Rechazada
                                intentosConfirmacion++
                            } else {
                                estadoCompra = EstadoCompra.Exitosa
                                carritoViewModel.limpiarCarrito(email)
                            }
                        }
                    )
                }
                EstadoCompra.Exitosa -> {
                    CompraRealizadaContent(navController)
                }
                EstadoCompra.Rechazada -> {
                    CompraRechazadaContent(
                        navController = navController,
                        onReintentar = { estadoCompra = EstadoCompra.EnProceso }
                    )
                }
            }
        }
    }
}

enum class EstadoCompra {
    EnProceso,
    Exitosa,
    Rechazada
}

@Composable
fun ResumenCompraContent(
    itemsCarrito: List<com.example.catalogoproductos.model.ItemCarrito>,
    totalCarrito: String,
    direccion: com.example.catalogoproductos.model.Direccion?,
    perfilFallback: PerfilUsuario?,
    onCompraClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Resumen de tu compra",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider()
            
            Text(
                text = "Productos:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            itemsCarrito.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.nombre} (${item.cantidad})",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(item.precio * item.cantidad),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = totalCarrito,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Dirección de envío",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider()
            
            when {
                direccion != null -> {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = "Calle: ${direccion.calle} ${direccion.numero}")
                        Text(text = "Ciudad: ${direccion.ciudad}, ${direccion.provincia}")
                        Text(text = "Código Postal: ${direccion.codigoPostal}")
                        Text(text = "Teléfono: ${direccion.telefono}")
                    }
                }
                perfilFallback != null -> {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = "Dirección: ${perfilFallback.direccion}")
                        Text(text = "Ciudad: ${perfilFallback.ciudad}")
                        Text(text = "Código Postal: ${perfilFallback.codigoPostal}")
                        Text(text = "Teléfono: ${perfilFallback.telefono}")
                        Text(
                            text = "Usando dirección del perfil por no existir una dirección predeterminada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "No hay dirección de envío seleccionada",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Método de pago",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "Tarjeta de crédito",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Pago contra entrega")
            }
        }
    }
    
    GradientButton(
        text = "Confirmar Compra",
        onClick = onCompraClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = itemsCarrito.isNotEmpty() && (direccion != null || perfilFallback != null),
        icon = Icons.Default.ShoppingCart
    )
}

@Composable
fun CompraRealizadaContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "¡Compra realizada con éxito!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Gracias por tu compra. Recibirás un correo con los detalles de tu pedido.",
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GradientButton(
            text = "Volver al Catálogo",
            onClick = { 
                navController.navigate("catalogo") {
                    popUpTo("catalogo") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Composable
fun CompraRechazadaContent(
    navController: NavController,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Compra rechazada",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Lo sentimos, no se pudo procesar tu pago. Por favor, verifica tus datos e intenta nuevamente.",
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GradientButton(
                text = "Reintentar",
                onClick = onReintentar,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(end = 8.dp)
            )
            
            GradientButton(
                text = "Cancelar",
                onClick = { 
                    navController.navigate("catalogo") {
                        popUpTo("catalogo") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}
