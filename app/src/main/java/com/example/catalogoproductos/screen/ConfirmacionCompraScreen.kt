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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.catalogoproductos.model.ItemCarrito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionCompraScreen(
    navController: NavController,
    carritoViewModel: CarritoViewModel,
    direccionViewModel: DireccionViewModel,
    email: String,
    perfilUsuarioViewModel: PerfilUsuarioViewModel,
    authViewModel: com.example.catalogoproductos.viewmodel.AuthViewModel
) {
    val scrollState = rememberScrollState()
    val itemsCarrito by carritoViewModel.itemsCarrito.collectAsState(initial = emptyList())
    val totalCarrito by carritoViewModel.totalCarrito.collectAsState(initial = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(0))
    val direccionDefault by direccionViewModel.direccionDefault.collectAsState(initial = null)
    val context = LocalContext.current

    LaunchedEffect(email) {
        direccionViewModel.cargarDireccionDefault(email)
    }
    LaunchedEffect(authViewModel.token.value) {
        val tk = authViewModel.token.value ?: ""
        if (tk.isNotBlank()) perfilUsuarioViewModel.cargarPerfilRemoto(tk)
    }
    
    val checkoutState by carritoViewModel.checkoutState.collectAsState()
    
    var estadoCompra by remember { mutableStateOf<EstadoCompra>(EstadoCompra.EnProceso) }
    var boletaItems by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }
    var boletaTotal by remember { mutableStateOf("") }
    var boletaNombre by remember { mutableStateOf("") }
    var boletaApellido by remember { mutableStateOf("") }
    var boletaDireccion by remember { mutableStateOf("") }
    
    // Estado para opciones de checkout
    var metodoEnvio by remember { mutableStateOf("domicilio") }
    var metodoPago by remember { mutableStateOf("tarjeta") }
    
    // Manejar estado del checkout
    LaunchedEffect(checkoutState) {
        when (val state = checkoutState) {
            is CarritoViewModel.CheckoutState.Success -> {
                // No sobrescribimos boletaItems aquí porque itemsCarrito ya podría estar vacío tras vaciarCarrito() en el VM.
                // Confiamos en que boletaItems se llenó correctamente en onCompraClick.
                
                estadoCompra = EstadoCompra.Exitosa
                carritoViewModel.resetCheckoutState()
            }
            is CarritoViewModel.CheckoutState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                estadoCompra = EstadoCompra.Rechazada // O mantener en proceso para reintentar
                carritoViewModel.resetCheckoutState()
            }
            else -> {}
        }
    }

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
            if (checkoutState is CarritoViewModel.CheckoutState.Loading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator()
                 }
            } else {
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
                            metodoEnvio = metodoEnvio,
                            onMetodoEnvioChange = { metodoEnvio = it },
                            metodoPago = metodoPago,
                            onMetodoPagoChange = { metodoPago = it },
                            onCompraClick = {
                                val token = authViewModel.token.value ?: ""
                                if (token.isBlank()) {
                                    Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                                    return@ResumenCompraContent
                                }

                                // Preparar datos para boleta (guardar copia local antes de limpiar)
                                boletaItems = itemsCarrito.toList()
                                boletaTotal = totalCarrito
                                boletaNombre = perfilUsuarioViewModel.nombre
                                boletaApellido = perfilUsuarioViewModel.apellido
                                boletaDireccion = when {
                                    direccionDefault != null -> {
                                        val d = direccionDefault!!
                                        listOfNotNull(d.calle, d.numero, d.ciudad, d.provincia, d.codigoPostal)
                                            .filter { it.isNotBlank() }
                                            .joinToString(", ")
                                    }
                                    perfilFallback != null -> {
                                        "${perfilFallback.direccion}, ${perfilFallback.ciudad}, ${perfilFallback.codigoPostal}"
                                    }
                                    else -> "Dirección no disponible"
                                }

                                // Determinar dirección para envío
                                val dirEnvio = direccionDefault?.calle ?: perfilFallback?.direccion ?: ""
                                val ciudadEnvio = direccionDefault?.ciudad ?: perfilFallback?.ciudad ?: ""
                                val regionEnvio = direccionDefault?.provincia ?: perfilFallback?.region ?: ""
                                val cpEnvio = direccionDefault?.codigoPostal ?: perfilFallback?.codigoPostal ?: ""
                                val destinatario = "${perfilUsuarioViewModel.nombre} ${perfilUsuarioViewModel.apellido}"

                                carritoViewModel.realizarCheckout(
                                    token = token,
                                    direccion = dirEnvio,
                                    region = regionEnvio,
                                    ciudad = ciudadEnvio,
                                    codigoPostal = cpEnvio,
                                    destinatario = destinatario,
                                    metodoPago = metodoPago,
                                    metodoEnvio = metodoEnvio
                                )
                            }
                        )
                    }
                    EstadoCompra.Exitosa -> {
                        CompraRealizadaContent(
                            navController = navController,
                            nombre = boletaNombre,
                            apellido = boletaApellido,
                            direccionEnvio = boletaDireccion,
                            items = boletaItems,
                            total = boletaTotal
                        )
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
    metodoEnvio: String,
    onMetodoEnvioChange: (String) -> Unit,
    metodoPago: String,
    onMetodoPagoChange: (String) -> Unit,
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

    // Selección de Método de Envío
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Método de Envío",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Column(Modifier.padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = metodoEnvio == "domicilio",
                        onClick = { onMetodoEnvioChange("domicilio") }
                    )
                    Text("Despacho a domicilio")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = metodoEnvio == "retiro",
                        onClick = { onMetodoEnvioChange("retiro") }
                    )
                    Text("Retiro en tienda")
                }
            }
        }
    }

    // Selección de Método de Pago
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Método de Pago",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Column(Modifier.padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = metodoPago == "tarjeta",
                        onClick = { onMetodoPagoChange("tarjeta") }
                    )
                    Text("Tarjeta de crédito/débito")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = metodoPago == "local",
                        onClick = { onMetodoPagoChange("local") }
                    )
                    Text("Pago en local")
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
                text = "Dirección de envío / Facturación",
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
                        text = "No tienes una dirección configurada. Ve a 'Mi Perfil' o 'Dirección de Envío' para agregar una.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
    
    GradientButton(
        text = "Confirmar Compra",
        onClick = onCompraClick,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CompraRealizadaContent(
    navController: NavController,
    nombre: String,
    apellido: String,
    direccionEnvio: String,
    items: List<ItemCarrito>,
    total: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Compra exitosa", Toast.LENGTH_SHORT).show()
        }
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
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Boleta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Nombre: $nombre $apellido")
                Text(text = "Dirección de envío: $direccionEnvio")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Detalle de productos", fontWeight = FontWeight.Bold)
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.nombre} x${item.cantidad}")
                        Text(text = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(item.precio * item.cantidad))
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total", fontWeight = FontWeight.Bold)
                    Text(text = total, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                // Desglose de IVA
                val totalInt = items.sumOf { it.precio * it.cantidad }
                val netFmt = com.example.catalogoproductos.util.CurrencyUtils.formatNetFromGrossCLP(totalInt)
                val taxFmt = com.example.catalogoproductos.util.CurrencyUtils.formatTaxFromGrossCLP(totalInt)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Precio sin IVA")
                        Text(text = netFmt)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "IVA (19%)")
                        Text(text = taxFmt)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Precio final con IVA", fontWeight = FontWeight.Bold)
                        Text(text = total, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

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
