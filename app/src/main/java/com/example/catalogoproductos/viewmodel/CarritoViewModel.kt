package com.example.catalogoproductos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.repository.CarritoRepository
import com.example.catalogoproductos.repository.CheckoutRepository
import com.example.catalogoproductos.network.CompraDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CarritoViewModel(
    private val carritoRepository: CarritoRepository,
    private val checkoutRepository: CheckoutRepository,
    private val emailUsuario: String
) : ViewModel() {

    sealed class CheckoutState {
        object Idle : CheckoutState()
        object Loading : CheckoutState()
        data class Success(val compra: CompraDTO) : CheckoutState()
        data class Error(val message: String) : CheckoutState()
    }

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _itemsCarrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val itemsCarrito: StateFlow<List<ItemCarrito>> = _itemsCarrito.asStateFlow()

    private val _totalCarrito = MutableStateFlow(NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build()).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(0))
    val totalCarrito: StateFlow<String> = _totalCarrito.asStateFlow()

    private val _cantidadItems = MutableStateFlow(0)
    val cantidadItems: StateFlow<Int> = _cantidadItems.asStateFlow()

    init {
        viewModelScope.launch {
            carritoRepository.getItemsCarrito(emailUsuario).collect { items ->
                _itemsCarrito.value = items
                calcularTotal(items)
                _cantidadItems.value = items.sumOf { it.cantidad }
            }
        }
    }

    private fun calcularTotal(items: List<ItemCarrito>) {
        val total = items.sumOf { it.precio * it.cantidad }
        val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build()).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        _totalCarrito.value = format.format(total)
    }

    fun agregarProductoAlCarrito(producto: Producto, cantidad: Int = 1) {
        viewModelScope.launch {
            val nuevoItem = ItemCarrito(
                productoId = producto.id,
                nombre = producto.nombre,
                precio = producto.precio,
                imagen = producto.imagen ?: "",
                cantidad = cantidad,
                emailUsuario = emailUsuario
            )
            carritoRepository.agregarAlCarrito(nuevoItem)
        }
    }
    
    fun agregarAlCarrito(item: ItemCarrito) {
        viewModelScope.launch {
            carritoRepository.agregarAlCarrito(item)
        }
    }

    fun actualizarCantidad(itemId: Int, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            eliminarItem(itemId)
            return
        }
        
        viewModelScope.launch {
            carritoRepository.actualizarCantidad(itemId, nuevaCantidad)
        }
    }

    fun eliminarItem(itemId: Int) {
        viewModelScope.launch {
            carritoRepository.eliminarItem(itemId)
        }
    }

    fun vaciarCarrito() {
        viewModelScope.launch {
            carritoRepository.vaciarCarrito(emailUsuario)
        }
    }
    
    fun limpiarCarrito(email: String) {
        viewModelScope.launch {
            carritoRepository.vaciarCarrito(email)
        }
    }

    fun realizarCheckout(
        token: String,
        direccion: String,
        region: String,
        ciudad: String,
        codigoPostal: String,
        destinatario: String,
        metodoPago: String,
        metodoEnvio: String
    ) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            try {
                val items = _itemsCarrito.value
                if (items.isEmpty()) {
                    _checkoutState.value = CheckoutState.Error("El carrito está vacío")
                    return@launch
                }
                
                val detalleItems = items.map {
                    com.example.catalogoproductos.network.DetalleOrdenRequest(
                        productoId = it.productoId.toString(),
                        nombre = it.nombre,
                        precioUnitario = it.precio.toDouble(),
                        cantidad = it.cantidad
                    )
                }
                
                val total = items.sumOf { it.precio * it.cantidad }.toDouble()
                
                val request = com.example.catalogoproductos.network.CheckoutRequest(
                    items = detalleItems,
                    total = total,
                    metodoEnvio = metodoEnvio,
                    metodoPago = metodoPago,
                    destinatario = destinatario,
                    direccion = direccion,
                    region = region,
                    ciudad = ciudad,
                    codigoPostal = codigoPostal
                )
                
                // 1. Crear Orden (Checkout)
                val orden = checkoutRepository.checkout(token, request)
                
                // 2. Confirmar Orden (Simulado inmediato)
                val compra = checkoutRepository.confirm(token, orden.id, "PAY-SIMULADO-${System.currentTimeMillis()}")
                
                // 3. Vaciar carrito local
                vaciarCarrito()
                
                _checkoutState.value = CheckoutState.Success(compra)
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(e.message ?: "Error en checkout")
            }
        }
    }
    
    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }

    class Factory(
        private val carritoRepository: CarritoRepository,
        private val checkoutRepository: CheckoutRepository,
        private val emailUsuario: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CarritoViewModel::class.java)) {
                return CarritoViewModel(carritoRepository, checkoutRepository, emailUsuario) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}