package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.Direccion
import com.example.catalogoproductos.repository.DireccionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DireccionViewModel(private val direccionRepository: DireccionRepository) : ViewModel() {

    // Estados para los campos del formulario
    var calle by mutableStateOf("")
        private set
    var numero by mutableStateOf("")
        private set
    var ciudad by mutableStateOf("")
        private set
    var provincia by mutableStateOf("")
        private set
    var codigoPostal by mutableStateOf("")
        private set
    var telefono by mutableStateOf("")
        private set
    var esDefault by mutableStateOf(false)
        private set

    // Estados para los errores de validación
    var calleError by mutableStateOf<String?>(null)
        private set
    var numeroError by mutableStateOf<String?>(null)
        private set
    var ciudadError by mutableStateOf<String?>(null)
        private set
    var provinciaError by mutableStateOf<String?>(null)
        private set
    var codigoPostalError by mutableStateOf<String?>(null)
        private set
    var telefonoError by mutableStateOf<String?>(null)
        private set

    // Estado para mensajes de error generales
    var errorMessage by mutableStateOf("")
        private set

    // Estado para indicar si se guardó correctamente
    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso.asStateFlow()

    // Dirección predeterminada del usuario
    private val _direccionDefault = MutableStateFlow<Direccion?>(null)
    val direccionDefault: StateFlow<Direccion?> = _direccionDefault.asStateFlow()

    // Funciones para actualizar los campos
    fun updateCalle(value: String) {
        calle = value
        validateCalle()
    }

    fun updateNumero(value: String) {
        numero = value
        validateNumero()
    }

    fun updateCiudad(value: String) {
        ciudad = value
        validateCiudad()
    }

    fun updateProvincia(value: String) {
        provincia = value
        validateProvincia()
    }

    fun updateCodigoPostal(value: String) {
        codigoPostal = value
        validateCodigoPostal()
    }

    fun updateTelefono(value: String) {
        telefono = value
        validateTelefono()
    }

    fun updateEsDefault(value: Boolean) {
        esDefault = value
    }

    // Funciones de validación
    private fun validateCalle() {
        calleError = when {
            calle.isEmpty() -> "La calle es obligatoria"
            calle.length < 3 -> "La calle debe tener al menos 3 caracteres"
            else -> null
        }
    }

    private fun validateNumero() {
        numeroError = when {
            numero.isEmpty() -> "El número es obligatorio"
            else -> null
        }
    }

    private fun validateCiudad() {
        ciudadError = when {
            ciudad.isEmpty() -> "La ciudad es obligatoria"
            ciudad.length < 3 -> "La ciudad debe tener al menos 3 caracteres"
            !ciudad.all { it.isLetter() || it.isWhitespace() } -> "La ciudad solo debe contener letras"
            else -> null
        }
    }

    private fun validateProvincia() {
        provinciaError = when {
            provincia.isEmpty() -> "La provincia es obligatoria"
            provincia.length < 3 -> "La provincia debe tener al menos 3 caracteres"
            !provincia.all { it.isLetter() || it.isWhitespace() } -> "La provincia solo debe contener letras"
            else -> null
        }
    }

    private fun validateCodigoPostal() {
        codigoPostalError = when {
            codigoPostal.isEmpty() -> "El código postal es obligatorio"
            codigoPostal.length != 5 -> "El código postal debe tener 5 dígitos"
            !codigoPostal.all { it.isDigit() } -> "El código postal solo debe contener números"
            else -> null
        }
    }

    private fun validateTelefono() {
        telefonoError = when {
            telefono.isEmpty() -> "El teléfono es obligatorio"
            telefono.length < 9 -> "El teléfono debe tener al menos 9 dígitos"
            !telefono.all { it.isDigit() } -> "El teléfono solo debe contener números"
            else -> null
        }
    }

    // Función para validar todos los campos
    private fun validateAllFields(): Boolean {
        validateCalle()
        validateNumero()
        validateCiudad()
        validateProvincia()
        validateCodigoPostal()
        validateTelefono()

        return calleError == null && numeroError == null && ciudadError == null &&
                provinciaError == null && codigoPostalError == null && telefonoError == null
    }

    // Cargar la dirección predeterminada del usuario
    fun cargarDireccionDefault(emailUsuario: String) {
        viewModelScope.launch {
            direccionRepository.getDireccionDefault(emailUsuario).collect { dir ->
                _direccionDefault.value = dir
            }
        }
    }

    // Función para guardar la dirección
    fun guardarDireccion(emailUsuario: String) {
        if (!validateAllFields()) {
            errorMessage = "Por favor, corrija los errores en el formulario"
            return
        }

        viewModelScope.launch {
            try {
                val direccion = Direccion(
                    emailUsuario = emailUsuario,
                    calle = calle,
                    numero = numero,
                    ciudad = ciudad,
                    provincia = provincia,
                    codigoPostal = codigoPostal,
                    telefono = telefono,
                    esDefault = esDefault
                )
                
                direccionRepository.guardarDireccion(direccion)
                _guardadoExitoso.value = true
                errorMessage = ""
            } catch (e: Exception) {
                errorMessage = "Error al guardar la dirección: ${e.message}"
                _guardadoExitoso.value = false
            }
        }
    }

    fun resetGuardadoExitoso() {
        _guardadoExitoso.value = false
    }
}

class Factory(private val direccionRepository: DireccionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DireccionViewModel::class.java)) {
            return DireccionViewModel(direccionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}