package com.example.catalogoproductos

import android.os.Bundle
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.catalogoproductos.database.AppDatabase
import com.example.catalogoproductos.repository.*
import com.example.catalogoproductos.screen.*
import com.example.catalogoproductos.ui.MiTopBar
import com.example.catalogoproductos.ui.theme.CatalogoProductosTheme
import com.example.catalogoproductos.viewmodel.*
import com.example.catalogoproductos.components.BottomNavigationBar
import com.example.catalogoproductos.viewmodel.NoticiasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatalogoProductosTheme {
                // Fondo degradado azul marino → negro
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    com.example.catalogoproductos.ui.theme.NavyBlue,
                                    Color.Black
                                )
                            )
                        )
                ) {
                    val navController = androidx.navigation.compose.rememberNavController()

                    // Base de datos y repositorios (recordados para evitar recreación en recomposición)
                    val db = remember { AppDatabase.getDatabase(applicationContext) }
                    val carritoRepository = remember { CarritoRepository(db.itemCarritoDao()) }
                    val perfilUsuarioRepository = remember { PerfilUsuarioRepository(db.perfilUsuarioDao()) }
                    val direccionRepository = remember { DireccionRepository(db.direccionDao()) }
                    val authViewModel = viewModel<AuthViewModel>()
                    val appCtx = applicationContext
                    val TOKEN = stringPreferencesKey("token")
                    val EMAIL = stringPreferencesKey("email")
                    val ROLE = stringPreferencesKey("role")
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val firstPrefs = appCtx.dataStore.data.first()
                        val tk = firstPrefs[TOKEN]
                        val email = firstPrefs[EMAIL]
                        val role = firstPrefs[ROLE]
                        if (!tk.isNullOrBlank() && !email.isNullOrBlank()) {
                            authViewModel.token.value = tk
                            authViewModel.usuarioActual.value = email
                            authViewModel.role.value = role?.uppercase()?.let {
                                when (it) {
                                    "ADMIN", "USER_AD", "PROD_AD", "CLIENT" -> it
                                    else -> "CLIENT"
                                }
                            }
                            authViewModel.esAdministrador.value = (authViewModel.role.value == "ADMIN")
                            try {
                                val me = com.example.catalogoproductos.repository.AuthRepository().me(tk)
                                val perfil = me.profile
                                if (perfil != null) {
                                    val local = com.example.catalogoproductos.model.PerfilUsuario(
                                        email = email,
                                        nombre = perfil.nombre ?: "",
                                        apellido = perfil.apellido ?: "",
                                        telefono = perfil.telefono ?: "",
                                        direccion = perfil.direccion ?: "",
                                        region = perfil.region ?: "",
                                        ciudad = perfil.ciudad ?: "",
                                        codigoPostal = perfil.codigoPostal ?: ""
                                    )
                                    perfilUsuarioRepository.guardarPerfil(local)
                                }
                            } catch (_: Exception) {
                                // Evitar crash si backend tarda o falla
                            }
                        }
                    }
                    androidx.compose.runtime.LaunchedEffect(authViewModel.token.value, authViewModel.usuarioActual.value, authViewModel.role.value) {
                        val tk = authViewModel.token.value
                        val email = authViewModel.usuarioActual.value
                        val role = authViewModel.role.value
                        appCtx.dataStore.edit { prefs ->
                            if (tk.isNullOrBlank() || email.isNullOrBlank()) {
                                prefs.remove(TOKEN)
                                prefs.remove(EMAIL)
                                prefs.remove(ROLE)
                            } else {
                                prefs[TOKEN] = tk
                                prefs[EMAIL] = email
                                if (role != null) prefs[ROLE] = role else prefs.remove(ROLE)
                            }
                        }
                    }
                    val catalogoViewModel = viewModel<CatalogoViewModel>()
                    val direccionViewModel = viewModel<DireccionViewModel>(
                        factory = com.example.catalogoproductos.viewmodel.Factory(direccionRepository)
                    )

                    var title by remember { mutableStateOf("Catálogo de Productos") }

                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            val showBack = currentRoute !in listOf("catalogo", "login", "noticias")
                            MiTopBar(
                                title = title,
                                navController = navController,
                                showBackButton = showBack,
                                isUserLoggedIn = authViewModel.usuarioActual.value != null
                            )
                        },
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            if (currentRoute !in listOf("login", "register")) {
                                BottomNavigationBar(
                                    navController = navController,
                                    isUserLoggedIn = authViewModel.usuarioActual.value != null,
                                    allowAdminProductos = authViewModel.canAccessBackofficeProductos(),
                                    allowAdminUsuarios = authViewModel.canAccessBackofficeUsuarios()
                                )
                            }
                        }
                    ) { paddingValues ->
                        NavHost(navController = navController, startDestination = "login", modifier = Modifier.padding(paddingValues)) {
                            composable("login") {
                                title = "Inicio de Sesión"
                                LoginScreen(navController, authViewModel)
                            }
                            composable("register") {
                                title = "Registro"
                                val registerViewModel = viewModel<RegisterViewModel>(
                                    factory = RegisterViewModel.Factory(perfilUsuarioRepository, applicationContext)
                                )
                                RegisterScreen(navController, registerViewModel)
                            }
                            composable("catalogo") {
                                title = "Catálogo de Productos"
                                CatalogoScreen(navController, catalogoViewModel, authViewModel)
                            }
                            composable("noticias") {
                                title = "Noticias"
                                val noticiasViewModel = viewModel<NoticiasViewModel>(
                                    factory = NoticiasViewModel.Factory { applicationContext.getString(R.string.gnews_api_key) }
                                )
                                com.example.catalogoproductos.screen.NoticiasScreen(navController, noticiasViewModel)
                            }
                            composable("detalle/{productoId}") { backStack ->
                                title = "Detalle del Producto"
                                val id =
                                    backStack.arguments?.getString("productoId")?.toIntOrNull() ?: -1
                                val email = authViewModel.usuarioActual.value
                                val carritoVm = if (email != null) {
                                    viewModel<CarritoViewModel>(
                                        factory = CarritoViewModel.Factory(
                                            carritoRepository,
                                            email
                                        )
                                    )
                                } else {
                                    viewModel<CarritoViewModel>(
                                        factory = CarritoViewModel.Factory(
                                            carritoRepository,
                                            ""
                                        )
                                    )
                                }
                                DetalleProductoScreen(
                                    id,
                                    catalogoViewModel,
                                    carritoVm,
                                    navController,
                                    authViewModel
                                )
                            }
                            composable("carrito") {
                                title = "Carrito de Compras"
                                val email = authViewModel.usuarioActual.value
                                email?.let { userEmail ->
                                    val carritoVm = viewModel<CarritoViewModel>(
                                        factory = CarritoViewModel.Factory(
                                            carritoRepository,
                                            userEmail
                                        )
                                    )
                                    CarritoScreen(navController, carritoVm)
                                } ?: run {
                                    // Si el usuario no está autenticado, redirigir al login
                                    navController.navigate("login")
                                }
                            }
                            composable("direccion") {
                                title = "Dirección de Envío"
                                // Use shared DireccionViewModel scoped to Activity
                                val email = authViewModel.usuarioActual.value
                                email?.let {
                                    DireccionScreen(navController, direccionViewModel, authViewModel)
                                } ?: run {
                                    // Si el usuario no está autenticado, redirigir al login
                                    navController.navigate("login")
                                }
                            }
                            composable("perfil") {
                                title = "Mi Perfil"
                                val email = authViewModel.usuarioActual.value
                                email?.let { userEmail ->
                                    val perfilViewModel = viewModel<PerfilUsuarioViewModel>(
                                        factory = PerfilUsuarioViewModel.Factory(
                                            perfilUsuarioRepository,
                                            userEmail
                                        )
                                    )
                                    PerfilUsuarioScreen(navController, perfilViewModel, authViewModel)
                                } ?: run {
                                    // Si el usuario no está autenticado, redirigir al login
                                    navController.navigate("login")
                                }
                            }
                            composable("confirmacion") {
                                title = "Confirmación de Compra"
                                val email = authViewModel.usuarioActual.value
                                // Use shared DireccionViewModel scoped to Activity
                                email?.let { userEmail ->
                                    val carritoVm = viewModel<CarritoViewModel>(
                                        factory = CarritoViewModel.Factory(
                                            carritoRepository,
                                            userEmail
                                        )
                                    )
                                    val perfilViewModel = viewModel<PerfilUsuarioViewModel>(
                                        factory = PerfilUsuarioViewModel.Factory(
                                            perfilUsuarioRepository,
                                            userEmail
                                        )
                                    )
                                    ConfirmacionCompraScreen(
                                        navController,
                                        carritoVm,
                                        direccionViewModel,
                                        userEmail,
                                        perfilViewModel,
                                        authViewModel
                                    )
                                } ?: run {
                                    // Si el usuario no está autenticado, redirigir al login
                                    navController.navigate("login")
                                }
                            }
                            composable("backoffice_productos") {
                                title = "Admin Productos"
                                val backOfficeViewModel = viewModel<BackOfficeViewModel>()
                                if (authViewModel.canAccessBackofficeProductos()) {
                                    BackOfficeScreen(navController, backOfficeViewModel, authViewModel)
                                } else {
                                    authViewModel.mensaje.value = "Acceso restringido: Admin Productos"
                                    navController.navigate("catalogo")
                                }
                            }
                            composable("backoffice_usuarios") {
                                title = "Admin Usuarios"
                                val usuariosViewModel = viewModel<UsuariosViewModel>()
                                if (authViewModel.canAccessBackofficeUsuarios()) {
                                    BackOfficeUsuariosScreen(navController, usuariosViewModel, authViewModel)
                                } else {
                                    authViewModel.mensaje.value = "Acceso restringido: Admin Usuarios"
                                    navController.navigate("catalogo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
val Context.dataStore by preferencesDataStore(name = "auth_prefs")
