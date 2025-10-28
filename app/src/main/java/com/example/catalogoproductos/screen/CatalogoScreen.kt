package com.example.catalogoproductos.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ExperimentalMaterial3Api
// Removed ExperimentalFoundationApi import as animateItemPlacement is no longer used
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
import coil.request.ImageRequest
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.catalogoproductos.components.GradientButton
import com.example.catalogoproductos.repository.CategoriaRepository
import androidx.compose.animation.animateContentSize

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = Color.Transparent,
        contentColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    action = {
                        data.visuals.actionLabel?.let { label ->
                            TextButton(onClick = { data.performAction() }) { Text(label, color = Color.White) }
                        }
                    }
                ) {
                    Text(data.visuals.message, color = Color.White)
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

            var searchText by rememberSaveable { mutableStateOf("") }
            var expanded by rememberSaveable { mutableStateOf(false) }
            var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
            var categorias by rememberSaveable { mutableStateOf(listOf("Todas")) }
            LaunchedEffect(Unit) {
                val repoCat = CategoriaRepository()
                val loaded = repoCat.obtenerCategoriasDesdeAssets(context)
                categorias = listOf("Todas") + loaded
            }
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar por nombre") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val productosPorCategoria = if (selectedCategory == "Todas") productos else productos.filter {
                it.tipo?.equals(selectedCategory, ignoreCase = true) ?: false
            }
            val productosFiltrados = productosPorCategoria.filter {
                searchText.isBlank() || it.nombre.contains(searchText, ignoreCase = true)
            }

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
                    items(productosFiltrados) { producto ->
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
                                    .animateContentSize()
                            ) {
                                // Imagen grande con precio arriba a la derecha
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                ) {
                                    val imageModel = ImageRequest.Builder(context)
                                        .data(producto.imagen ?: "")
                                        .crossfade(true)
                                        .build()
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageModel),
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
                                AnimatedVisibility(
                                    visible = descripcion.isNotBlank(),
                                    enter = fadeIn(animationSpec = tween(250)),
                                    exit = fadeOut(animationSpec = tween(200))
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = descripcion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Botón abajo a la derecha
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    GradientButton(
                                        text = "Ver detalle",
                                        onClick = { navController.navigate("detalle/${producto.id}") },
                                        modifier = Modifier.width(160.dp).height(45.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
