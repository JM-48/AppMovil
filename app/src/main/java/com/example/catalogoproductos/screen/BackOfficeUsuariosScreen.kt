package com.example.catalogoproductos.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import com.example.catalogoproductos.repository.UsuarioRepository
import com.example.catalogoproductos.repository.RegionComunaRepository
import com.example.catalogoproductos.viewmodel.AuthViewModel
import com.example.catalogoproductos.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackOfficeUsuariosScreen(
    navController: NavController,
    viewModel: UsuariosViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val usuarios by viewModel.usuarios.collectAsState()
    val seleccionado by viewModel.usuarioSeleccionado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Usuarios", "Crear usuario", "Editar usuario")

    LaunchedEffect(authViewModel.token.value) {
        val tk = authViewModel.token.value
        if (!tk.isNullOrBlank()) {
            viewModel.cargarUsuarios(tk)
        }
    }

    LaunchedEffect(viewModel.statusMessage) {
        val msg = viewModel.statusMessage
        if (msg.isNotBlank()) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = tabIndex, containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) }, selected = tabIndex == index, onClick = {
                        when (index) {
                            0 -> tabIndex = 0
                            1 -> { viewModel.nuevoUsuario(); tabIndex = 1 }
                            2 -> { if (seleccionado != null) tabIndex = 2 else Toast.makeText(context, "Selecciona un usuario para editar", Toast.LENGTH_SHORT).show() }
                        }
                    }, icon = { Icon(imageVector = when (index) { 0 -> Icons.Default.List; else -> Icons.Default.Edit }, contentDescription = title) })
                }
            }

            when (tabIndex) {
                0 -> UsuariosTab(
                    usuarios = usuarios,
                    onEdit = { viewModel.seleccionarUsuario(it); tabIndex = 2 },
                    onDelete = {
                        val tk = authViewModel.token.value ?: ""
                        if (tk.isNotBlank() && it.id != null) viewModel.eliminarUsuario(tk, it.id!!) else Toast.makeText(context, "Token o ID inválido", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> UsuarioForm(viewModel = viewModel, isCreate = true, tokenProvider = { authViewModel.token.value ?: "" })
                2 -> if (seleccionado != null) UsuarioForm(viewModel = viewModel, isCreate = false, tokenProvider = { authViewModel.token.value ?: "" }) else Text(text = "Selecciona un usuario para editar", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun UsuariosTab(
    usuarios: List<UsuarioRepository.UsuarioDto>,
    onEdit: (UsuarioRepository.UsuarioDto) -> Unit,
    onDelete: (UsuarioRepository.UsuarioDto) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(usuarios) { u ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onEdit(u) }, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = (u.nombre ?: "").ifBlank { "Sin nombre" })
                        Text(text = (u.apellido ?: "").ifBlank { "" })
                        Text(text = u.email ?: "")
                        Text(text = "Rol: ${u.role ?: ""}")
                        val tel = u.telefono ?: ""
                        if (tel.isNotBlank()) Text(text = "Teléfono: $tel")
                        val dir = u.direccion ?: ""
                        val city = u.ciudad ?: ""
                        val cp = u.codigoPostal ?: ""
                        val addr = listOf(dir, city, cp).filter { it.isNotBlank() }.joinToString(", ")
                        if (addr.isNotBlank()) Text(text = addr)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onEdit(u) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                        val isAdmin = u.role?.equals("ADMIN", ignoreCase = true) == true
                        if (!isAdmin) {
                            IconButton(onClick = { onDelete(u) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                        } else {
                            IconButton(onClick = { /* bloqueado */ }, enabled = false) { Icon(Icons.Default.Delete, contentDescription = "Eliminar (bloqueado)") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioForm(viewModel: UsuariosViewModel, isCreate: Boolean, tokenProvider: () -> String) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.updateNombre(it) }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), isError = viewModel.nombreError != null, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), singleLine = true)
        viewModel.nombreError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(value = viewModel.apellido, onValueChange = { viewModel.updateApellido(it) }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), singleLine = true)

        OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.updateEmail(it) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), isError = viewModel.emailError != null, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), singleLine = true)
        viewModel.emailError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(value = viewModel.password, onValueChange = { viewModel.updatePassword(it) }, label = { Text(if (isCreate) "Contraseña" else "Nueva contraseña (opcional)") }, modifier = Modifier.fillMaxWidth(), isError = viewModel.passwordError != null, visualTransformation = PasswordVisualTransformation(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next), singleLine = true)
        viewModel.passwordError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(value = viewModel.telefono, onValueChange = { viewModel.updateTelefono(it) }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next), singleLine = true)

        OutlinedTextField(value = viewModel.direccion, onValueChange = { viewModel.updateDireccion(it) }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), singleLine = true)

        val regionesYComunas = remember { RegionComunaRepository().cargarDesdeAssets(context) }
        var selectedRegion by remember { mutableStateOf(viewModel.region) }
        var expandedRegion by remember { mutableStateOf(false) }
        var expandedComuna by remember { mutableStateOf(false) }
        val regiones = remember(regionesYComunas) { regionesYComunas.map { it.region }.filter { it.isNotBlank() } }
        val comunas = remember(selectedRegion) { regionesYComunas.firstOrNull { it.region == selectedRegion }?.comunas?.filter { it.isNotBlank() } ?: emptyList() }

        ExposedDropdownMenuBox(expanded = expandedRegion, onExpandedChange = { expandedRegion = !expandedRegion }) {
            OutlinedTextField(value = selectedRegion, onValueChange = {}, readOnly = true, label = { Text("Región") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = expandedRegion, onDismissRequest = { expandedRegion = false }) {
                regiones.forEach { r ->
                    DropdownMenuItem(text = { Text(r) }, onClick = {
                        selectedRegion = r
                        viewModel.updateRegion(r)
                        val firstComuna = regionesYComunas.firstOrNull { it.region == r }?.comunas?.firstOrNull { it.isNotBlank() } ?: ""
                        viewModel.updateCiudad(firstComuna)
                        expandedRegion = false
                    })
                }
            }
        }

        ExposedDropdownMenuBox(expanded = expandedComuna, onExpandedChange = { expandedComuna = !expandedComuna }) {
            OutlinedTextField(value = viewModel.ciudad, onValueChange = {}, readOnly = true, label = { Text("Comuna") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedComuna) }, modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true)
            ExposedDropdownMenu(expanded = expandedComuna, onDismissRequest = { expandedComuna = false }) {
                comunas.forEach { c ->
                    DropdownMenuItem(text = { Text(c) }, onClick = {
                        viewModel.updateCiudad(c)
                        expandedComuna = false
                    })
                }
            }
        }

        OutlinedTextField(value = viewModel.codigoPostal, onValueChange = { viewModel.updateCodigoPostal(it) }, label = { Text("Código Postal") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true)

        var expandedRol by remember { mutableStateOf(false) }
        val roles = listOf("ADMIN", "USER_AD", "PROD_AD", "CLIENT", "VENDEDOR")
        ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = !expandedRol }) {
            OutlinedTextField(value = viewModel.role, onValueChange = {}, readOnly = true, label = { Text("Rol") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }, modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true, isError = viewModel.roleError != null)
            ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                roles.forEach { r ->
                    DropdownMenuItem(text = { Text(r) }, onClick = { viewModel.updateRole(r); expandedRol = false })
                }
            }
        }
        viewModel.roleError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val tk = tokenProvider()
                viewModel.guardarUsuario(tk)
            }) { Text("Guardar") }
            OutlinedButton(onClick = { viewModel.nuevoUsuario() }) { Text("Limpiar") }
        }

        LaunchedEffect(viewModel.statusMessage) {
            val msg = viewModel.statusMessage
            if (msg.isNotBlank()) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
