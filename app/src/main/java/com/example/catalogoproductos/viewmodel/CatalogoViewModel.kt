package com.example.catalogoproductos.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.repository.ProductoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel(
    private val repo: ProductoRepository = ProductoRepository()
) : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    fun cargarProductos(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val list = repo.obtenerProductosDesdeApi()
                _productos.value = list
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun refreshProductos(context: Context) {
        viewModelScope.launch {
            _refreshing.value = true
            _error.value = null
            try {
                val list = repo.obtenerProductosDesdeApi()
                _productos.value = list
            } catch (e: Exception) {
                _error.value = "Error al actualizar productos: ${e.message}"
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun buscarProductoPorId(id: Int): Producto? =
        _productos.value.find { it.id == id }
}
