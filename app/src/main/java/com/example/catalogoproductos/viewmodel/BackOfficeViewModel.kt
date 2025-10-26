package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.model.FakeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackOfficeViewModel : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado.asStateFlow()

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso.asStateFlow()

    // Campos del formulario
    var nombre by mutableStateOf("")
        private set
    var descripcion by mutableStateOf("")
        private set
    var precio by mutableStateOf("")
        private set
    var stock by mutableStateOf("")
        private set
    var imagen by mutableStateOf("")
        private set
    // El modelo actual no tiene categoría, se elimina su estado
    // var categoria by mutableStateOf("")

    // Estados de error
    var nombreError by mutableStateOf<String?>(null)
        private set
    var descripcionError by mutableStateOf<String?>(null)
        private set
    var precioError by mutableStateOf<String?>(null)
        private set
    var stockError by mutableStateOf<String?>(null)
        private set
    var imagenError by mutableStateOf<String?>(null)
        private set
    // var categoriaError by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf("")
        private set

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        _productos.value = FakeDatabase.getProductos()
    }

    fun seleccionarProducto(producto: Producto) {
        _productoSeleccionado.value = producto
        nombre = producto.nombre
        descripcion = producto.descripcion ?: ""
        precio = producto.precio.toString()
        stock = producto.stock.toString()
        imagen = producto.imagen ?: ""
        limpiarErrores()
    }

    fun nuevoProducto() {
        _productoSeleccionado.value = null
        nombre = ""
        descripcion = ""
        precio = ""
        stock = ""
        imagen = ""
        limpiarErrores()
    }

    fun updateNombre(value: String) { nombre = value; validarNombre() }
    fun updateDescripcion(value: String) { descripcion = value; validarDescripcion() }
    fun updatePrecio(value: String) { precio = value; validarPrecio() }
    fun updateStock(value: String) { stock = value; validarStock() }
    fun updateImagen(value: String) { imagen = value; validarImagen() }

    fun guardarProducto() {
        if (validarFormulario()) {
            try {
                val precioInt = precio.toInt()
                val stockInt = stock.toInt()

                val producto = Producto(
                    id = _productoSeleccionado.value?.id ?: 0,
                    nombre = nombre,
                    descripcion = if (descripcion.isBlank()) null else descripcion,
                    precio = precioInt,
                    imagen = if (imagen.isBlank()) null else imagen,
                    stock = stockInt
                )

                if (_productoSeleccionado.value == null) {
                    FakeDatabase.agregarProducto(producto)
                } else {
                    FakeDatabase.actualizarProducto(producto)
                }

                _guardadoExitoso.value = true
                cargarProductos()
            } catch (e: Exception) {
                errorMessage = "Error al guardar el producto: ${e.message}"
            }
        }
    }

    fun eliminarProducto(id: Int) {
        FakeDatabase.eliminarProducto(id)
        cargarProductos()
        if (_productoSeleccionado.value?.id == id) {
            nuevoProducto()
        }
    }

    fun resetGuardadoExitoso() { _guardadoExitoso.value = false }

    private fun validarFormulario(): Boolean {
        limpiarErrores()
        var isValid = true
        if (!validarNombre()) isValid = false
        if (!validarDescripcion()) isValid = false
        if (!validarPrecio()) isValid = false
        if (!validarStock()) isValid = false
        if (!validarImagen()) isValid = false
        return isValid
    }

    private fun validarNombre(): Boolean {
        return when {
            nombre.isBlank() -> { nombreError = "El nombre es obligatorio"; false }
            nombre.length < 3 -> { nombreError = "El nombre debe tener al menos 3 caracteres"; false }
            else -> { nombreError = null; true }
        }
    }

    private fun validarDescripcion(): Boolean {
        return when {
            descripcion.isBlank() -> { descripcionError = null; true } // opcional
            descripcion.length < 10 -> { descripcionError = "La descripción debe tener al menos 10 caracteres"; false }
            else -> { descripcionError = null; true }
        }
    }

    private fun validarPrecio(): Boolean {
        val p = precio.toIntOrNull()
        return when {
            precio.isBlank() -> { precioError = "El precio es obligatorio"; false }
            p == null -> { precioError = "El precio debe ser un entero"; false }
            p <= 0 -> { precioError = "El precio debe ser mayor que cero"; false }
            else -> { precioError = null; true }
        }
    }

    private fun validarStock(): Boolean {
        val s = stock.toIntOrNull()
        return when {
            stock.isBlank() -> { stockError = "El stock es obligatorio"; false }
            s == null -> { stockError = "El stock debe ser un número entero"; false }
            s <= 0 -> { stockError = "El stock debe ser mayor que cero"; false }
            else -> { stockError = null; true }
        }
    }

    private fun validarImagen(): Boolean {
        return when {
            imagen.isBlank() -> { imagenError = "La URL de la imagen es obligatoria"; false }
            !imagen.startsWith("http") -> { imagenError = "La URL debe comenzar con http:// o https://"; false }
            else -> { imagenError = null; true }
        }
    }

    private fun limpiarErrores() {
        nombreError = null
        descripcionError = null
        precioError = null
        stockError = null
        imagenError = null
        errorMessage = ""
    }
}