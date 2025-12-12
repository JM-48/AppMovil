package com.example.catalogoproductos.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.example.catalogoproductos.repository.PerfilUsuarioRepository
import com.example.catalogoproductos.repository.AuthRepository
import com.example.catalogoproductos.repository.RegionComunaRepository
import com.example.catalogoproductos.model.PerfilUsuario
import com.example.catalogoproductos.model.FakeDatabase
import com.example.catalogoproductos.model.Usuario
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel(
    private val perfilRepo: PerfilUsuarioRepository,
    private val appContext: Context
) : ViewModel() {
    var nombre by mutableStateOf("")
    var apellido by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var direccion by mutableStateOf("")
    var telefono by mutableStateOf("")
    var codigoPostal by mutableStateOf("")

    var nombreError by mutableStateOf<String?>(null)
    var apellidoError by mutableStateOf<String?>(null)
    var emailError by mutableStateOf<String?>(null)
    var passwordError by mutableStateOf<String?>(null)
    var confirmPasswordError by mutableStateOf<String?>(null)
    var direccionError by mutableStateOf<String?>(null)
    var telefonoError by mutableStateOf<String?>(null)
    var codigoPostalError by mutableStateOf<String?>(null)

    var region by mutableStateOf("")
    var comuna by mutableStateOf("")
    var regionError by mutableStateOf<String?>(null)
    var comunaError by mutableStateOf<String?>(null)
    var registroExitoso by mutableStateOf(false)
    var mensaje by mutableStateOf("")

    private val regionComunaRepo = RegionComunaRepository()
    val regionesYComunas = regionComunaRepo.cargarDesdeAssets(appContext)

    fun validateNombre() {
        nombreError = when {
            nombre.isBlank() -> "El nombre es obligatorio"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            !nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
            else -> null
        }
    }
    fun validateApellido() {
        apellidoError = when {
            apellido.isBlank() -> "El apellido es obligatorio"
            apellido.length < 2 -> "El apellido debe tener al menos 2 caracteres"
            !apellido.all { it.isLetter() || it.isWhitespace() } -> "El apellido solo debe contener letras"
            else -> null
        }
    }
    fun validateEmail() {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        emailError = when {
            email.isEmpty() -> "El email es obligatorio"
            !emailPattern.matcher(email).matches() -> "Email inválido"
            else -> null
        }
    }
    fun validatePassword() {
        passwordError = when {
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !password.any { it.isDigit() } -> "Debe incluir al menos un número"
            !password.any { it.isUpperCase() } -> "Debe incluir una mayúscula"
            else -> null
        }
    }
    fun validateConfirmPassword() {
        confirmPasswordError = if (confirmPassword != password) "Las contraseñas no coinciden" else null
    }
    fun validateDireccion() {
        direccionError = when {
            direccion.isBlank() -> "La dirección es obligatoria"
            direccion.length < 5 -> "La dirección debe tener al menos 5 caracteres"
            else -> null
        }
    }
    fun validateTelefono() {
        telefonoError = when {
            telefono.isBlank() -> "El teléfono es obligatorio"
            telefono.length < 9 -> "El teléfono debe tener al menos 9 dígitos"
            !telefono.all { it.isDigit() } -> "El teléfono solo debe contener números"
            else -> null
        }
    }
    fun validateCodigoPostal() {
        codigoPostalError = when {
            codigoPostal.isBlank() -> "El código postal es obligatorio"
            codigoPostal.length != 5 || !codigoPostal.all { it.isDigit() } -> "El código postal debe tener 5 dígitos"
            else -> null
        }
    }

    fun validateRegion() {
        regionError = if (region.isBlank()) "Selecciona una región" else null
    }
    fun validateComuna() {
        comunaError = if (comuna.isBlank()) "Selecciona una comuna" else null
    }

    fun updateRegion(value: String) {
        region = value
        validateRegion()
        // Reset comuna cuando cambia la región
        comuna = ""
        comunaError = null
    }
    fun updateComuna(value: String) {
        comuna = value
        validateComuna()
    }

    // Métodos de actualización
    fun updateNombre(value: String) {
        nombre = value
        validateNombre()
    }
    fun updateApellido(value: String) {
        apellido = value
        validateApellido()
    }
    fun updateEmail(value: String) {
        email = value
        validateEmail()
    }
    fun updatePassword(value: String) {
        password = value
        validatePassword()
    }
    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        validateConfirmPassword()
    }
    fun updateDireccion(value: String) {
        direccion = value
        validateDireccion()
    }
    fun updateTelefono(value: String) {
        telefono = value
        validateTelefono()
    }
    fun updateCodigoPostal(value: String) {
        codigoPostal = value
        validateCodigoPostal()
    }

    fun register() {
        validateNombre()
        validateApellido()
        validateEmail()
        validatePassword()
        validateConfirmPassword()
        validateDireccion()
        validateTelefono()
        validateCodigoPostal()
        validateRegion()
        validateComuna()

        val noErrors = listOf(
            nombreError, apellidoError, emailError, passwordError, confirmPasswordError,
            direccionError, telefonoError, codigoPostalError, regionError, comunaError
        ).all { it == null }

        if (!noErrors) {
            mensaje = "Formulario inválido"
            registroExitoso = false
            return
        }

        // Consumir endpoint /auth/register
        val req = AuthRepository.RegisterRequest(
            email = email,
            password = password,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            direccion = direccion,
            ciudad = comuna,
            region = region,
            codigoPostal = codigoPostal,
            role = "USER"
        )
        viewModelScope.launch {
            try {
                val authRepo = AuthRepository()
                authRepo.register(req)
                mensaje = "Registro exitoso"
                registroExitoso = true
            } catch (e: Exception) {
                emailError = (e.message ?: "Error de registro").replace("\n", " ")
                mensaje = emailError ?: "Error de registro"
                registroExitoso = false
                return@launch
            }

            val perfil = PerfilUsuario(
                email = email,
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                direccion = direccion,
                ciudad = comuna,
                codigoPostal = codigoPostal
            )
            perfilRepo.guardarPerfil(perfil)
        }
    }

    class Factory(
        private val perfilRepo: PerfilUsuarioRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(perfilRepo, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
