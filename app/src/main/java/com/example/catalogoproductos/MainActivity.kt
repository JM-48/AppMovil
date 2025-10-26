package com.example.catalogoproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.catalogoproductos.components.BottomNavigationBar
import com.example.catalogoproductos.database.AppDatabase
import com.example.catalogoproductos.repository.CarritoRepository
import com.example.catalogoproductos.repository.DireccionRepository
import com.example.catalogoproductos.repository.PerfilUsuarioRepository
import com.example.catalogoproductos.screen.*
import com.example.catalogoproductos.ui.MiTopBar
import com.example.catalogoproductos.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val catalogoViewModel = remember { CatalogoViewModel() }
            val authViewModel = remember { AuthViewModel() }
            
            // Inicializar la base de datos y repositorios
            val database = AppDatabase.getDatabase(this)
            val carritoRepository = CarritoRepository(database.itemCarritoDao())
            val perfilUsuarioRepository = PerfilUsuarioRepository(database.perfilUsuarioDao())
            val direccionRepository = DireccionRepository(database.direccionDao())
            
            // Inicializar ViewModels
            // CarritoViewModel se crea por ruta con Factory usando email
            
            var title by remember { mutableStateOf("Login") }
            
            // Determinar si el usuario está autenticado
            val isUserLoggedIn = authViewModel.usuarioActual.value != null
            val isAdmin = authViewModel.esAdministrador.value
            
            // Determinar si mostrar el bottom navigation
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomNav = when (currentRoute) {
                "login", "register" -> false
                else -> true
            }

            Scaffold(
                topBar = {
                    MiTopBar(
                        title = title,
                        navController = navController,
                        showBackButton = currentRoute != "catalogo" && currentRoute != "login",
                        showCartButton = currentRoute != "carrito" && currentRoute != "login" && currentRoute != "register",
                        isUserLoggedIn = isUserLoggedIn
                    )
                },
                bottomBar = {
                    if (showBottomNav) {
                        BottomNavigationBar(
                            navController = navController,
                            isUserLoggedIn = isUserLoggedIn,
                            isAdmin = isAdmin
                        )
                    }
                }
            ) { innerPadding ->
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            title = "Login"
                            LoginScreen(navController, authViewModel)
                        }
                        composable("register") {
                            title = "Registro"
                            val registerViewModel = RegisterViewModel(perfilUsuarioRepository)
                            RegisterScreen(navController, registerViewModel)
                        }
                        composable("catalogo") {
                            title = "Catálogo de Productos"
                            CatalogoScreen(navController, catalogoViewModel, authViewModel)
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
                            val direccionViewModel = DireccionViewModel(direccionRepository)
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
                            val direccionViewModel = DireccionViewModel(direccionRepository)
                            email?.let { userEmail ->
                                val carritoVm = viewModel<CarritoViewModel>(
                                    factory = CarritoViewModel.Factory(
                                        carritoRepository,
                                        userEmail
                                    )
                                )
                                ConfirmacionCompraScreen(
                                    navController,
                                    carritoVm,
                                    direccionViewModel,
                                    userEmail
                                )
                            } ?: run {
                                // Si el usuario no está autenticado, redirigir al login
                                navController.navigate("login")
                            }
                        }
                        composable("backoffice") {
                            title = "Back Office - Administración"
                            val backOfficeViewModel = viewModel<BackOfficeViewModel>()
                            if (authViewModel.esAdministrador.value) {
                                BackOfficeScreen(navController, backOfficeViewModel)
                            } else {
                                // Si no es administrador, redirigir al login
                                navController.navigate("login")
                            }
                        }
                    }
                }
            }
        }
    }
}