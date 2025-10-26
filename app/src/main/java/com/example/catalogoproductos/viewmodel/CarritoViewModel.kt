package com.example.catalogoproductos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.repository.CarritoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CarritoViewModel(
    private val carritoRepository: CarritoRepository,
    private val emailUsuario: String
) : ViewModel() {

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

    class Factory(
        private val carritoRepository: CarritoRepository,
        private val emailUsuario: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CarritoViewModel::class.java)) {
                return CarritoViewModel(carritoRepository, emailUsuario) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}