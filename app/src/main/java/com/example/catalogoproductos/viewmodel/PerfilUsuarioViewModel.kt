package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.PerfilUsuario
import com.example.catalogoproductos.repository.PerfilUsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(
    private val perfilUsuarioRepository: PerfilUsuarioRepository,
    userEmail: String
) : ViewModel() {

    // Estados para los campos del formulario
    var email by mutableStateOf(userEmail)
        private set
    var nombre by mutableStateOf("")
        private set
    var apellido by mutableStateOf("")
        private set
    var telefono by mutableStateOf("")
        private set
    var direccion by mutableStateOf("")
        private set
    var ciudad by mutableStateOf("")
        private set
    var codigoPostal by mutableStateOf("")
        private set

    // Estados para los errores de validación
    var nombreError by mutableStateOf<String?>(null)
        private set
    var apellidoError by mutableStateOf<String?>(null)
        private set
    var telefonoError by mutableStateOf<String?>(null)
        private set
    var direccionError by mutableStateOf<String?>(null)
        private set
    var ciudadError by mutableStateOf<String?>(null)
        private set
    var codigoPostalError by mutableStateOf<String?>(null)
        private set

    // Estado para mensajes de error generales
    var errorMessage by mutableStateOf("")
        private set

    // Estado para indicar si el guardado fue exitoso
    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            // Collect the Flow<PerfilUsuario?> and update state when data arrives
            perfilUsuarioRepository.getPerfilUsuario(email).collect { perfil ->
                perfil?.let {
                    nombre = it.nombre
                    apellido = it.apellido
                    telefono = it.telefono
                    direccion = it.direccion
                    ciudad = it.ciudad
                    codigoPostal = it.codigoPostal
                }
            }
        }
    }

    fun updateNombre(value: String) {
        nombre = value
        validarNombre()
    }

    fun updateApellido(value: String) {
        apellido = value
        validarApellido()
    }

    fun updateTelefono(value: String) {
        telefono = value
        validarTelefono()
    }

    fun updateDireccion(value: String) {
        direccion = value
        validarDireccion()
    }

    fun updateCiudad(value: String) {
        ciudad = value
        validarCiudad()
    }

    fun updateCodigoPostal(value: String) {
        codigoPostal = value
        validarCodigoPostal()
    }

    private fun validarNombre() {
        nombreError = when {
            nombre.isBlank() -> "El nombre es obligatorio"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            !nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
            else -> null
        }
    }

    private fun validarApellido() {
        apellidoError = when {
            apellido.isBlank() -> "El apellido es obligatorio"
            apellido.length < 2 -> "El apellido debe tener al menos 2 caracteres"
            !apellido.all { it.isLetter() || it.isWhitespace() } -> "El apellido solo debe contener letras"
            else -> null
        }
    }

    private fun validarTelefono() {
        telefonoError = when {
            telefono.isBlank() -> "El teléfono es obligatorio"
            telefono.length < 9 -> "El teléfono debe tener al menos 9 dígitos"
            !telefono.all { it.isDigit() } -> "El teléfono solo debe contener números"
            else -> null
        }
    }

    private fun validarDireccion() {
        direccionError = when {
            direccion.isBlank() -> "La dirección es obligatoria"
            direccion.length < 5 -> "La dirección debe tener al menos 5 caracteres"
            else -> null
        }
    }

    private fun validarCiudad() {
        ciudadError = when {
            ciudad.isBlank() -> "La ciudad es obligatoria"
            ciudad.length < 3 -> "La ciudad debe tener al menos 3 caracteres"
            !ciudad.all { it.isLetter() || it.isWhitespace() } -> "La ciudad solo debe contener letras"
            else -> null
        }
    }

    private fun validarCodigoPostal() {
        codigoPostalError = when {
            codigoPostal.isBlank() -> "El código postal es obligatorio"
            codigoPostal.length != 5 || !codigoPostal.all { it.isDigit() } -> "El código postal debe tener 5 dígitos"
            else -> null
        }
    }

    private fun validarFormulario(): Boolean {
        validarNombre()
        validarApellido()
        validarTelefono()
        validarDireccion()
        validarCiudad()
        validarCodigoPostal()

        return nombreError == null && apellidoError == null && telefonoError == null &&
                direccionError == null && ciudadError == null && codigoPostalError == null
    }

    fun guardarPerfil() {
        if (validarFormulario()) {
            viewModelScope.launch {
                try {
                    val perfilUsuario = PerfilUsuario(
                        email = email,
                        nombre = nombre,
                        apellido = apellido,
                        telefono = telefono,
                        direccion = direccion,
                        ciudad = ciudad,
                        codigoPostal = codigoPostal
                    )
                    // Use repository's Spanish method name
                    perfilUsuarioRepository.guardarPerfil(perfilUsuario)
                    _guardadoExitoso.value = true
                    errorMessage = ""
                } catch (e: Exception) {
                    errorMessage = "Error al guardar el perfil: ${e.message}"
                    _guardadoExitoso.value = false
                }
            }
        } else {
            errorMessage = "Por favor, corrija los errores en el formulario"
        }
    }

    fun resetGuardadoExitoso() {
        _guardadoExitoso.value = false
    }

    class Factory(
        private val perfilUsuarioRepository: PerfilUsuarioRepository,
        private val userEmail: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PerfilUsuarioViewModel(perfilUsuarioRepository, userEmail) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}