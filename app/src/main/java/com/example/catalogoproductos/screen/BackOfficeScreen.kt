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
import androidx.compose.foundation.rememberScrollState
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
import com.example.catalogoproductos.viewmodel.AuthViewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackOfficeScreen(
    navController: NavController,
    viewModel: BackOfficeViewModel,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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

    // Mostrar mensaje de inicio de sesión de administrador al entrar
    LaunchedEffect(authViewModel.mensaje.value, authViewModel.usuarioActual.value, authViewModel.esAdministrador.value) {
        val msg = authViewModel.mensaje.value
        if (authViewModel.usuarioActual.value != null && authViewModel.esAdministrador.value && msg.isNotBlank()) {
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            authViewModel.mensaje.value = ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
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
}

@Composable
fun ProductosTab(
    productos: List<Producto>,
    onProductoClick: (Producto) -> Unit,
    onDeleteClick: (Producto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(productos) { producto ->
            ProductoItem(producto, onProductoClick, onDeleteClick)
        }
    }
}

@Composable
fun ProductoItem(
    producto: Producto,
    onProductoClick: (Producto) -> Unit,
    onDeleteClick: (Producto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductoClick(producto) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = producto.nombre, fontWeight = FontWeight.Bold)
                Text(text = "Precio: ${com.example.catalogoproductos.util.CurrencyUtils.formatCLP(producto.precio)}")
                Text(text = "Stock: ${producto.stock}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onProductoClick(producto) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onDeleteClick(producto) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun FormularioTab(viewModel: BackOfficeViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Formulario de Producto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.nombre,
            onValueChange = { viewModel.updateNombre(it) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.nombreError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        viewModel.nombreError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.descripcion,
            onValueChange = { viewModel.updateDescripcion(it) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.precio,
            onValueChange = { viewModel.updatePrecio(it) },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.precioError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        viewModel.precioError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.stock,
            onValueChange = { viewModel.updateStock(it) },
            label = { Text("Stock") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModel.stockError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        viewModel.stockError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.imagen,
            onValueChange = { viewModel.updateImagen(it) },
            label = { Text("URL de imagen") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.guardarProducto() }) {
                Text("Guardar")
            }
            OutlinedButton(onClick = { viewModel.nuevoProducto() }) {
                Text("Limpiar")
            }
        }
    }
}
