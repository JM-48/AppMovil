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
import androidx.compose.ui.platform.LocalContext
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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import android.util.Log
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackOfficeScreen(
    navController: NavController,
    viewModel: BackOfficeViewModel,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val productos by viewModel.productos.collectAsState()
    val productoSeleccionado by viewModel.productoSeleccionado.collectAsState()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estado para controlar qué pestaña está activa
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Productos", "Crear producto", "Editar producto")
    
    // Estado para el diálogo de confirmación de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    // Cargar productos desde assets al abrir BackOffice
    LaunchedEffect(Unit) {
        viewModel.cargarProductosDesdeAssets(context)
    }

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

    LaunchedEffect(viewModel.productoStatusMessage) {
        val msg = viewModel.productoStatusMessage
        if (msg.isNotBlank()) {
            val cleanMsg = msg.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
            Toast.makeText(context, cleanMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearProductoStatusMessage()
        }
    }

    // Mostrar mensaje de inicio de sesión de administrador al entrar
    LaunchedEffect(authViewModel.mensaje.value, authViewModel.usuarioActual.value, authViewModel.esAdministrador.value) {
        val msg = authViewModel.mensaje.value
        if (authViewModel.usuarioActual.value != null && authViewModel.esAdministrador.value && msg.isNotBlank()) {
            val cleanMsg = msg.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
            Toast.makeText(context, cleanMsg, Toast.LENGTH_SHORT).show()
            authViewModel.mensaje.value = ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showDeleteDialog && productoAEliminar != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.eliminarProducto(productoAEliminar!!.id)
                            showDeleteDialog = false
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                    },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Deseas eliminar '${productoAEliminar!!.nombre}'?") }
                )
            }
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
                        onClick = {
                            when (index) {
                                0 -> tabIndex = 0
                                1 -> {
                                    viewModel.nuevoProducto()
                                    tabIndex = 1
                                }
                                2 -> {
                                    val seleccionado = productoSeleccionado
                                    if (seleccionado != null) {
                                        tabIndex = 2
                                    } else {
                                        Toast.makeText(context, "Selecciona un producto para editar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.List
                                    1 -> Icons.Default.Edit
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
                        tabIndex = 2
                    },
                    onDeleteClick = { 
                        productoAEliminar = it
                        showDeleteDialog = true
                    }
                )
                1 -> FormularioTab(viewModel = viewModel, title = "Crear Producto", isCreate = true)
                2 -> {
                    val seleccionado by viewModel.productoSeleccionado.collectAsState()
                    if (seleccionado != null) {
                        FormularioTab(viewModel = viewModel, title = "Editar Producto", isCreate = false)
                    } else {
                        Text(
                            text = "Selecciona un producto en la lista para editar",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioTab(viewModel: BackOfficeViewModel, title: String, isCreate: Boolean) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var tempImageFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageFile?.let { file ->
                viewModel.updateImagenFile(file)
                viewModel.updateImagen("")
                viewModel.subirImagenCapturada()
            }
        }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "galeria_${System.currentTimeMillis()}.jpg")
                input?.use { ins ->
                    file.outputStream().use { outs ->
                        ins.copyTo(outs)
                    }
                }
                viewModel.updateImagenFile(file)
                viewModel.updateImagen("")
                viewModel.subirImagenCapturada()
            } catch (_: Exception) { }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        
        var formatoDescripcion by remember { mutableStateOf("Markdown") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { formatoDescripcion = "Markdown" }, label = { Text("Markdown") }, leadingIcon = { if (formatoDescripcion == "Markdown") Icon(Icons.Default.Check, null) })
            AssistChip(onClick = { formatoDescripcion = "HTML" }, label = { Text("HTML") }, leadingIcon = { if (formatoDescripcion == "HTML") Icon(Icons.Default.Check, null) })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { viewModel.updateDescripcion("**" + viewModel.descripcion + "**") }) { Icon(Icons.Default.FormatBold, contentDescription = "Negrita") }
            IconButton(onClick = { viewModel.updateDescripcion("_" + viewModel.descripcion + "_") }) { Icon(Icons.Default.FormatItalic, contentDescription = "Cursiva") }
            IconButton(onClick = { viewModel.updateDescripcion("# " + viewModel.descripcion) }) { Icon(Icons.Default.Title, contentDescription = "Título") }
            IconButton(onClick = { viewModel.updateDescripcion(viewModel.descripcion + "\n- ") }) { Icon(Icons.Default.List, contentDescription = "Lista") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.descripcion,
            onValueChange = { viewModel.updateDescripcion(it) },
            label = { Text("Descripción (${formatoDescripcion})") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            maxLines = 6
        )
        Spacer(modifier = Modifier.height(8.dp))
        fun markdownToHtml(md: String): String {
            var s = md
            s = s.replace(Regex("^# (.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
            s = s.replace(Regex("^## (.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
            s = s.replace(Regex("\n- (.*)", RegexOption.MULTILINE), "<br/>• $1")
            s = s.replace(Regex("""\*\*(.*?)\*\*""", RegexOption.DOT_MATCHES_ALL), "<b>$1</b>")
            s = s.replace(Regex("_(.*?)_", RegexOption.DOT_MATCHES_ALL), "<i>$1</i>")
            return s
        }
        val htmlPreview = if (formatoDescripcion == "HTML") viewModel.descripcion else markdownToHtml(viewModel.descripcion)
        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.ui.viewinterop.AndroidView(factory = { ctx -> android.widget.TextView(ctx) }, update = { tv ->
                val sp = androidx.core.text.HtmlCompat.fromHtml(htmlPreview, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
                tv.text = sp
                tv.setTextColor(android.graphics.Color.WHITE)
            }, modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        val productosState by viewModel.productos.collectAsState()
        val categorias = productosState.mapNotNull { it.tipo }.filter { it.isNotBlank() }.distinct().sorted()
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = viewModel.tipo,
                onValueChange = { viewModel.updateTipo(it) },
                readOnly = false,
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                singleLine = true,
                enabled = categorias.isNotEmpty()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val filtered = if (viewModel.tipo.isBlank()) categorias else categorias.filter { it.contains(viewModel.tipo, ignoreCase = true) }
                filtered.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria) },
                        onClick = {
                            viewModel.updateTipo(categoria)
                            expanded = false
                        }
                    )
                }
            }
        }
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                val file = File(context.cacheDir, "captura_${System.currentTimeMillis()}.jpg")
                tempImageFile = file
                val uri = FileProvider.getUriForFile(context, "com.example.catalogoproductos.fileprovider", file)
                takePictureLauncher.launch(uri)
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar foto")
            }
            IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, contentDescription = "Elegir de galería")
            }
            IconButton(onClick = {
                viewModel.updateImagenFile(null)
                viewModel.updateImagen("")
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar imagen")
            }
        }
        LaunchedEffect(viewModel.imagenUploadMessage) {
            val msg = viewModel.imagenUploadMessage
            if (msg.isNotBlank()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearImagenUploadMessage()
            }
        }
        LaunchedEffect(viewModel.productoStatusMessage) {
            val msg = viewModel.productoStatusMessage
            if (msg.isNotBlank()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearProductoStatusMessage()
            }
        }
        fun normalizeUrl(u: String?): String {
            if (u.isNullOrBlank()) return ""
            return if (u.startsWith("http://") || u.startsWith("https://")) u else "https://apitest-1-95ny.onrender.com/" + if (u.startsWith("/")) u.drop(1) else u
        }
        val previewImage = when {
            viewModel.imagen.isNotBlank() -> normalizeUrl(viewModel.imagen)
            viewModel.imagenFile != null -> viewModel.imagenFile!!.absolutePath
            else -> null
        }
        previewImage?.let { pathOrUrl ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = pathOrUrl),
                    contentDescription = "Preview imagen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { 
                Log.d("BackOffice", "Botón Guardar presionado ($title)")
                if (isCreate) {
                    viewModel.crearProducto()
                } else {
                    viewModel.guardarProducto()
                }
            }) {
                Text("Guardar")
            }
            OutlinedButton(onClick = { viewModel.nuevoProducto() }) {
                Text("Limpiar")
            }
        }
    }
}
