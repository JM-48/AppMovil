package com.example.catalogoproductos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiTopBar(
    title: String,
    navController: NavController? = null,
    showBackButton: Boolean = false,
    showCartButton: Boolean = false,
    isUserLoggedIn: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = { 
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    val popped = navController?.popBackStack() ?: false
                    if (!popped) {
                        // Fallback: si no hay historial, navegar a una ruta segura
                        val fallbackRoute = if (isUserLoggedIn) "catalogo" else "login"
                        navController?.navigate(fallbackRoute) {
                            popUpTo(fallbackRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = {
            if (showCartButton && navController != null) {
                IconButton(onClick = { navController.navigate("carrito") }) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Ver carrito"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}