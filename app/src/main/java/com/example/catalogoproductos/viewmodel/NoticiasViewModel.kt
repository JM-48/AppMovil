package com.example.catalogoproductos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.Noticia
import com.example.catalogoproductos.repository.NoticiasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoticiasViewModel(
    private val repo: NoticiasRepository = NoticiasRepository(),
    private val apiKeyProvider: () -> String = { "" }
) : ViewModel() {
    private val _noticias = MutableStateFlow<List<Noticia>>(emptyList())
    val noticias: StateFlow<List<Noticia>> = _noticias

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var query: String = "gaming"
        private set

    fun updateQuery(value: String) { query = value }

    fun buscarNoticias() {
        val key = apiKeyProvider()
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = repo.buscar(query, lang = "es", max = 10, apiKey = key)
                _noticias.value = res
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    class Factory(private val apiKeyProvider: () -> String = { "" }) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NoticiasViewModel(NoticiasRepository(), apiKeyProvider) as T
        }
    }
}
