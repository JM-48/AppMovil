package com.example.catalogoproductos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "DriftX",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
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
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.secondary,
            actionIconContentColor = MaterialTheme.colorScheme.secondary
        )
    )
}
