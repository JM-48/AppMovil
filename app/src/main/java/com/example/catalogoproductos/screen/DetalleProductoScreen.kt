package com.example.catalogoproductos.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.viewmodel.CatalogoViewModel
import com.example.catalogoproductos.viewmodel.CarritoViewModel
import com.example.catalogoproductos.viewmodel.AuthViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.example.catalogoproductos.components.GradientButton

@Composable
fun DetalleProductoScreen(
    productoId: Int,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val producto = catalogoViewModel.buscarProductoPorId(productoId)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val itemsCarrito by carritoViewModel.itemsCarrito.collectAsState()

    // Usar email string en vez de objeto usuario
    val currentUserEmail = authViewModel.usuarioActual.value

    if (producto == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Producto no encontrado",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Volver",
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = producto.imagen),
            contentDescription = producto.nombre,
            modifier = Modifier.size(200.dp)
        )
        Text(
            text = producto.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        val descripcion = producto.descripcion ?: ""
        if (descripcion.isNotBlank()) {
            fun markdownToHtml(md: String): String {
                var s = md
                s = s.replace(Regex("^# (.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
                s = s.replace(Regex("^## (.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
                s = s.replace(Regex("\n- (.*)", RegexOption.MULTILINE), "<br/>• $1")
                s = s.replace(Regex("\*\*(.*?)\*\*", RegexOption.DOT_MATCHES_ALL), "<b>$1</b>")
                s = s.replace(Regex("_(.*?)_", RegexOption.DOT_MATCHES_ALL), "<i>$1</i>")
                return s
            }
            val html = if (descripcion.contains("<")) descripcion else markdownToHtml(descripcion)
            androidx.compose.ui.viewinterop.AndroidView(factory = { ctx -> android.widget.TextView(ctx) }, update = { tv ->
                val sp = androidx.core.text.HtmlCompat.fromHtml(html, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
                tv.text = sp
                tv.setTextColor(android.graphics.Color.WHITE)
            })
        }
        Text(
            text = "Precio: ${com.example.catalogoproductos.util.CurrencyUtils.formatCLP(producto.precio)}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        val qtyEnCarrito = itemsCarrito.firstOrNull { it.productoId == producto.id }?.cantidad ?: 0
        GradientButton(
            text = "Agregar al Carrito",
            onClick = {
            currentUserEmail?.let { email ->
                if (producto.stock <= 0) {
                    scope.launch { snackbarHostState.showSnackbar("Sin stock disponible") }
                } else if (qtyEnCarrito + 1 > producto.stock) {
                    scope.launch { snackbarHostState.showSnackbar("No puedes comprar más que el stock disponible") }
                } else {
                    val item = ItemCarrito(
                        productoId = producto.id,
                        nombre = producto.nombre,
                        precio = producto.precio,
                        imagen = producto.imagen ?: "",
                        cantidad = 1,
                        emailUsuario = email
                    )
                    carritoViewModel.agregarAlCarrito(item)
                    scope.launch { snackbarHostState.showSnackbar("Producto agregado al carrito") }
                }
            } ?: run {
                scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para agregar al carrito") }
            }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = producto.stock > 0
        )

        GradientButton(
            text = "Ir al Carrito",
            onClick = {
            currentUserEmail?.let { _ ->
                navController.navigate("carrito")
            } ?: run {
                scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para ver tu carrito") }
            }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        SnackbarHost(hostState = snackbarHostState)
    }
}
