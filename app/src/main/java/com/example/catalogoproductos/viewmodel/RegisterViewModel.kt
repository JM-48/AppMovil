package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.model.FakeDatabase
import com.example.catalogoproductos.model.PerfilUsuario
import com.example.catalogoproductos.model.Usuario
import com.example.catalogoproductos.repository.PerfilUsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel(private val perfilUsuarioRepository: PerfilUsuarioRepository? = null) : ViewModel() {

    // Estados para los campos del formulario
    var nombre by mutableStateOf("")
        private set
    var apellido by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var telefono by mutableStateOf("")
        private set
    var rut by mutableStateOf("")
        private set
    var direccion by mutableStateOf("")
        private set

    // Estados para los errores de validación
    var nombreError by mutableStateOf<String?>(null)
        private set
    var apellidoError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set
    var telefonoError by mutableStateOf<String?>(null)
        private set
    var rutError by mutableStateOf<String?>(null)
        private set
    var direccionError by mutableStateOf<String?>(null)
        private set

    // Estado para mostrar/ocultar contraseña
    var passwordVisible by mutableStateOf(false)
        private set
    var confirmPasswordVisible by mutableStateOf(false)
        private set

    // Estado para el resultado del registro
    private val _registrationResult = MutableStateFlow<RegistrationResult?>(null)
    val registrationResult: StateFlow<RegistrationResult?> = _registrationResult.asStateFlow()

    // Funciones para actualizar los campos
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
        if (confirmPassword.isNotEmpty()) {
            validateConfirmPassword()
        }
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        validateConfirmPassword()
    }

    fun updateTelefono(value: String) {
        telefono = value
        validateTelefono()
    }

    fun updateRut(value: String) {
        rut = value
        validateRut()
    }

    fun updateDireccion(value: String) {
        direccion = value
        validateDireccion()
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible
    }

    // Funciones de validación
    private fun validateNombre() {
        nombreError = when {
            nombre.isEmpty() -> "El nombre es obligatorio"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            !nombre.all { it.isLetter() || it.isWhitespace() } -> "El nombre solo debe contener letras"
            else -> null
        }
    }

    private fun validateApellido() {
        apellidoError = when {
            apellido.isEmpty() -> "El apellido es obligatorio"
            apellido.length < 2 -> "El apellido debe tener al menos 2 caracteres"
            !apellido.all { it.isLetter() || it.isWhitespace() } -> "El apellido solo debe contener letras"
            else -> null
        }
    }

    private fun validateEmail() {
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
            !emailPattern.matcher(email).matches() -> "El email no es válido"
            else -> null
        }
    }

    private fun validatePassword() {
        passwordError = when {
            password.isEmpty() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !password.any { it.isDigit() } -> "La contraseña debe contener al menos un número"
            !password.any { it.isUpperCase() } -> "La contraseña debe contener al menos una mayúscula"
            else -> null
        }
    }

    private fun validateConfirmPassword() {
        confirmPasswordError = when {
            confirmPassword.isEmpty() -> "Debe confirmar la contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    private fun validateTelefono() {
        telefonoError = when {
            telefono.isEmpty() -> "El teléfono es obligatorio"
            telefono.length < 8 -> "El teléfono debe tener al menos 8 dígitos"
            !telefono.all { it.isDigit() } -> "El teléfono solo debe contener números"
            else -> null
        }
    }
    
    private fun validateDireccion() {
        direccionError = when {
            direccion.isBlank() -> "La dirección es obligatoria"
            direccion.length < 5 -> "La dirección debe tener al menos 5 caracteres"
            else -> null
        }
    }
    
    private fun validateRut() {
        rutError = when {
            rut.isEmpty() -> "El RUT es obligatorio"
            !isValidRut(rut) -> "El RUT no es válido"
            else -> null
        }
    }
    
    // Función para validar RUT chileno
    private fun isValidRut(rut: String): Boolean {
        // Eliminar puntos y guión
        val cleanRut = rut.replace(".", "").replace("-", "").trim()
        
        // Verificar longitud mínima
        if (cleanRut.length < 2) return false
        
        try {
            val dv = cleanRut.last().toString()
            val rutNumber = cleanRut.dropLast(1).toInt()
            
            // Calcular dígito verificador
            return dv.equals(calculateDv(rutNumber), ignoreCase = true)
        } catch (e: Exception) {
            return false
        }
    }
    
    // Calcular dígito verificador
    private fun calculateDv(rut: Int): String {
        var suma = 0
        var factor = 2
        var rutTemp = rut
        
        while (rutTemp > 0) {
            suma += (rutTemp % 10) * factor
            rutTemp /= 10
            factor = if (factor == 7) 2 else factor + 1
        }
        
        val resultado = 11 - (suma % 11)
        return when (resultado) {
            11 -> "0"
            10 -> "K"
            else -> resultado.toString()
        }
    }

    // Función para validar todos los campos
    private fun validateAllFields(): Boolean {
        validateNombre()
        validateEmail()
        validatePassword()
        validateConfirmPassword()
        validateRut()
        validateDireccion()

        return nombreError == null && emailError == null &&
                passwordError == null && confirmPasswordError == null &&
                rutError == null && direccionError == null
    }

    // Función para registrar al usuario
    fun register() {
        if (!validateAllFields()) {
            _registrationResult.value = RegistrationResult.Error("Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            try {
                // Registrar en FakeDatabase
                val result = FakeDatabase.registrar(Usuario(nombre, email, password))
                
                if (result) {
                    // Si el registro fue exitoso, guardar el perfil en Room si está disponible
                    perfilUsuarioRepository?.let {
                        val perfil = PerfilUsuario(
                            email = email,
                            nombre = nombre,
                            apellido = apellido,
                            telefono = telefono,
                            direccion = direccion,
                            ciudad = "",
                            codigoPostal = ""
                        )
                        it.guardarPerfil(perfil)
                    }
                    
                    _registrationResult.value = RegistrationResult.Success
                } else {
                    _registrationResult.value = RegistrationResult.Error("El email ya está registrado")
                }
            } catch (e: Exception) {
                _registrationResult.value = RegistrationResult.Error("Error al registrar: ${e.message}")
            }
        }
    }

    fun resetRegistrationResult() {
        _registrationResult.value = null
    }

    // Clase sellada para el resultado del registro
    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
    }
}