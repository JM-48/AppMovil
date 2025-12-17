package com.example.catalogoproductos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.network.OrdenDTO
import com.example.catalogoproductos.repository.OrdenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdminOrdenesViewModel(
    private val repository: OrdenRepository,
    private val token: String
) : ViewModel() {

    private val _ordenes = MutableStateFlow<List<OrdenDTO>>(emptyList())
    val ordenes: StateFlow<List<OrdenDTO>> = _ordenes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        cargarTodasLasOrdenes()
    }

    fun cargarTodasLasOrdenes() {
        if (token.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getAllOrders(token)
                .catch { e ->
                    _error.value = e.message ?: "Error al cargar órdenes"
                    _isLoading.value = false
                }
                .collect { list ->
                    _ordenes.value = list
                    _isLoading.value = false
                }
        }
    }

    fun actualizarOrden(
        ordenId: Int, 
        nuevoEstado: String,
        destinatario: String,
        direccion: String,
        region: String,
        ciudad: String,
        codigoPostal: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateOrder(
                    token, 
                    ordenId, 
                    nuevoEstado,
                    destinatario,
                    direccion,
                    region,
                    ciudad,
                    codigoPostal
                )
                _mensaje.value = "Orden actualizada correctamente"
                cargarTodasLasOrdenes() // Recargar lista
            } catch (e: Exception) {
                _error.value = "Error al actualizar orden: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    class Factory(
        private val repository: OrdenRepository,
        private val token: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminOrdenesViewModel::class.java)) {
                return AdminOrdenesViewModel(repository, token) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
