package com.example.catalogoproductos.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.viewmodel.BackOfficeViewModel

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackOfficeScreen(
    navController: NavController,
    viewModel: BackOfficeViewModel
) {
    val productos by viewModel.productos.collectAsState()
    val productoSeleccionado by viewModel.productoSeleccionado.collectAsState()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estado para controlar qué pestaña está activa
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Productos", "Formulario")
    
    // Estado para el diálogo de confirmación de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            snackbarHostState.showSnackbar(
                message = "Producto guardado exitosamente",
                actionLabel = "OK"
            )
            viewModel.resetGuardadoExitoso()
            // Cambiar a la pestaña de productos después de guardar
            tabIndex = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back Office - Administración") },
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarProductos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (tabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        viewModel.nuevoProducto()
                        tabIndex = 1
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir producto")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pestañas
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.List
                                    else -> Icons.Default.Edit
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }
            
            // Contenido según la pestaña seleccionada
            when (tabIndex) {
                0 -> ProductosTab(
                    productos = productos,
                    onProductoClick = { 
                        viewModel.seleccionarProducto(it)
                        tabIndex = 1
                    },
                    onDeleteClick = { 
                        productoAEliminar = it
                        showDeleteDialog = true
                    }
                )
                1 -> FormularioTab(viewModel = viewModel)
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro que desea eliminar el producto '${productoAEliminar?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        productoAEliminar?.id?.let { viewModel.eliminarProducto(it) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProductosTab(
    productos: List<Producto>,
    onProductoClick: (Producto) -> Unit,
    onDeleteClick: (Producto) -> Unit
) {
    if (productos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Sin productos",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No hay productos disponibles")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos) { producto ->
                ProductoAdminCard(
                    producto = producto,
                    onProductoClick = { onProductoClick(producto) },
                    onDeleteClick = { onDeleteClick(producto) }
                )
            }
        }
    }
}

@Composable
fun ProductoAdminCard(
    producto: Producto,
    onProductoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductoClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${producto.id}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = com.example.catalogoproductos.util.CurrencyUtils.formatCLP(producto.precio),
                     fontWeight = FontWeight.Medium,
                     color = MaterialTheme.colorScheme.primary
                 )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stock: ${producto.stock}",
                    fontSize = 14.sp
                )
            }
            
            // Botones de acción
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onProductoClick) {
                    Text("Editar", color = MaterialTheme.colorScheme.primary)
                }

            }
        }
    }
}

@Composable
fun FormularioTab(viewModel: BackOfficeViewModel) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (viewModel.productoSeleccionado.value != null) "Editar Producto" else "Nuevo Producto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Nombre
        OutlinedTextField(
            value = viewModel.nombre,
            onValueChange = { viewModel.updateNombre(it) },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.nombreError != null,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            singleLine = true
        )
        viewModel.nombreError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
        
        // Descripción
        OutlinedTextField(
            value = viewModel.descripcion,
            onValueChange = { viewModel.updateDescripcion(it) },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.descripcionError != null,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            maxLines = 3
        )
        viewModel.descripcionError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
        
        // Precio (entero)
        OutlinedTextField(
            value = viewModel.precio,
            onValueChange = { viewModel.updatePrecio(it) },
            label = { Text("Precio (entero)") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.precioError != null,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true
        )
        viewModel.precioError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
        
        // Stock (> 0)
        OutlinedTextField(
            value = viewModel.stock,
            onValueChange = { viewModel.updateStock(it) },
            label = { Text("Stock (mayor que 0)") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.stockError != null,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true
        )
        viewModel.stockError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
        
        // URL de la imagen
        OutlinedTextField(
            value = viewModel.imagen,
            onValueChange = { viewModel.updateImagen(it) },
            label = { Text("URL de la imagen") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.imagenError != null,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            singleLine = true
        )
        viewModel.imagenError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
        
        // Mensaje de error general
        if (viewModel.errorMessage.isNotEmpty()) {
            Text(text = viewModel.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }
        
        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { viewModel.nuevoProducto() }, modifier = Modifier.weight(1f)) { Text("Limpiar") }
            Button(onClick = {}, modifier = Modifier.weight(1f), enabled = false) { Text("Guardar (no funcional)") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}