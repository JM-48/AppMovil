package com.example.catalogoproductos.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val requiresAuth: Boolean = false
)

@Composable
fun BottomNavigationBar(
    navController: NavController,
    isUserLoggedIn: Boolean,
    allowAdminProductos: Boolean,
    allowAdminUsuarios: Boolean
) {
    val baseItems = listOf(
        BottomNavItem(
            name = "Catálogo",
            route = "catalogo",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = "Noticias",
            route = "noticias",
            icon = Icons.Default.List
        ),
        BottomNavItem(
            name = "Carrito",
            route = "carrito",
            icon = Icons.Default.ShoppingCart
        ),
        BottomNavItem(
            name = "Perfil",
            route = "perfil",
            icon = Icons.Default.Person,
            requiresAuth = true
        )
    )

    val adminItems = buildList {
        if (allowAdminProductos) add(
            BottomNavItem(
                name = "Admin Productos",
                route = "backoffice_productos",
                icon = Icons.Default.Work
            )
        )
        if (allowAdminUsuarios) add(
            BottomNavItem(
                name = "Admin Usuarios",
                route = "backoffice_usuarios",
                icon = Icons.Default.Person
            )
        )
    }
    val items = baseItems + adminItems

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.Transparent) {
        items.forEach { item ->
            // Si el elemento requiere autenticación y el usuario no está autenticado, no lo mostramos
            if (!item.requiresAuth || isUserLoggedIn) {
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.name) },
                    label = { Text(text = item.name) },
                    selected = currentRoute == item.route,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Evitar múltiples copias de la misma ruta en la pila
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}
