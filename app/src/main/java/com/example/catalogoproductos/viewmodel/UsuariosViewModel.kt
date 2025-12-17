package com.example.catalogoproductos.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel : ViewModel() {
    private val _usuarios = MutableStateFlow<List<UsuarioRepository.UsuarioDto>>(emptyList())
    val usuarios: StateFlow<List<UsuarioRepository.UsuarioDto>> = _usuarios.asStateFlow()

    private val _usuarioSeleccionado = MutableStateFlow<UsuarioRepository.UsuarioDto?>(null)
    val usuarioSeleccionado: StateFlow<UsuarioRepository.UsuarioDto?> = _usuarioSeleccionado.asStateFlow()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var nombre by mutableStateOf("")
    var apellido by mutableStateOf("")
    var telefono by mutableStateOf("")
    var direccion by mutableStateOf("")
    var ciudad by mutableStateOf("")
    var codigoPostal by mutableStateOf("")
    var region by mutableStateOf("")
    var role by mutableStateOf("CLIENT")

    var emailError by mutableStateOf<String?>(null)
    var passwordError by mutableStateOf<String?>(null)
    var nombreError by mutableStateOf<String?>(null)
    var roleError by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf("")
    var statusMessage by mutableStateOf("")

    private fun limpiarErrores() {
        emailError = null
        passwordError = null
        nombreError = null
        roleError = null
        errorMessage = ""
    }

    fun cargarUsuarios(token: String) {
        viewModelScope.launch {
            try {
                val repo = UsuarioRepository()
                _usuarios.value = repo.listar(token)
            } catch (e: Exception) {
                errorMessage = "Error al cargar usuarios: ${e.message}"
            }
        }
    }

    fun seleccionarUsuario(u: UsuarioRepository.UsuarioDto) {
        _usuarioSeleccionado.value = u
        email = u.email ?: ""
        nombre = u.nombre ?: ""
        apellido = u.apellido ?: ""
        telefono = u.telefono ?: ""
        direccion = u.direccion ?: ""
        ciudad = u.ciudad ?: ""
        codigoPostal = u.codigoPostal ?: ""
        role = normalizeRole(u.role)
        password = ""
        limpiarErrores()
    }

    fun nuevoUsuario() {
        _usuarioSeleccionado.value = null
        email = ""
        nombre = ""
        apellido = ""
        telefono = ""
        direccion = ""
        ciudad = ""
        codigoPostal = ""
        password = ""
        role = "CLIENT"
        limpiarErrores()
    }

    fun updateEmail(v: String) { email = v; validateEmail() }
    fun updateNombre(v: String) { nombre = v; validateNombre() }
    fun updatePassword(v: String) { password = v; validatePassword() }
    fun updateApellido(v: String) { apellido = v }
    fun updateTelefono(v: String) { telefono = v }
    fun updateDireccion(v: String) { direccion = v }
    fun updateCiudad(v: String) { ciudad = v }
    fun updateRegion(v: String) { region = v }
    fun updateCodigoPostal(v: String) { codigoPostal = v }
    fun updateRole(v: String) { role = normalizeRole(v); validateRole() }

    private fun normalizeRole(input: String?): String {
        val r = input?.uppercase()?.trim()
        return when (r) {
            null, "" -> ""
            "ADMIN", "USER_AD", "PROD_AD", "CLIENT", "VENDEDOR" -> r
            "USER" -> "CLIENT"
            else -> r ?: ""
        }
    }

    private fun validateEmail(): Boolean {
        return when {
            email.isBlank() -> { emailError = "El email es obligatorio"; false }
            !email.contains('@') -> { emailError = "Email inválido"; false }
            else -> { emailError = null; true }
        }
    }
    private fun validateNombre(): Boolean {
        return when {
            nombre.isBlank() -> { nombreError = "El nombre es obligatorio"; false }
            nombre.length < 2 -> { nombreError = "El nombre debe tener al menos 2 caracteres"; false }
            else -> { nombreError = null; true }
        }
    }
    private fun validatePassword(): Boolean {
        if (_usuarioSeleccionado.value != null && password.isBlank()) { passwordError = null; return true }
        return when {
            password.length < 6 -> { passwordError = "La contraseña debe tener al menos 6 caracteres"; false }
            else -> { passwordError = null; true }
        }
    }
    private fun validateRole(): Boolean {
        if (role.isBlank()) { roleError = "El rol es obligatorio"; return false }
        return when (normalizeRole(role)) {
            "ADMIN", "USER_AD", "PROD_AD", "CLIENT", "VENDEDOR" -> { roleError = null; true }
            else -> { roleError = "Rol inválido"; false }
        }
    }

    private fun validarFormulario(): Boolean {
        limpiarErrores()
        var ok = true
        if (!validateEmail()) ok = false
        if (!validateNombre()) ok = false
        if (!validatePassword()) ok = false
        if (!validateRole()) ok = false
        if (!ok) {
            val errs = listOf(emailError, nombreError, passwordError, roleError).filterNotNull().joinToString(" | ")
            statusMessage = if (errs.isBlank()) "Formulario inválido" else "Formulario inválido: $errs"
            Log.d("BackOfficeUsuarios", "Validación fallida: $errs")
        }
        return ok
    }

    fun guardarUsuario(token: String) {
        if (token.isBlank()) { statusMessage = "Token de administrador no disponible"; return }
        if (!validarFormulario()) return
        viewModelScope.launch {
            try {
                Log.d("BackOfficeUsuarios", "GuardarUsuario: Inicio. Email=$email, Role=$role")
                val repo = UsuarioRepository()
                val body = mutableMapOf<String, Any?>(
                    "email" to email,
                    "password" to (if (password.isBlank()) null else password),
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "telefono" to telefono,
                    "direccion" to direccion,
                    "ciudad" to ciudad,
                    "region" to region,
                    "codigoPostal" to codigoPostal,
                    "role" to role
                )
                val sel = _usuarioSeleccionado.value
                val result = if (sel == null) {
                    Log.d("BackOfficeUsuarios", "GuardarUsuario: Creando nuevo usuario")
                    repo.crear(token, body)
                } else {
                    Log.d("BackOfficeUsuarios", "GuardarUsuario: Actualizando usuario id=${sel.id}")
                    repo.actualizar(token, sel.id ?: 0, body)
                }
                val created = sel == null
                _usuarios.value = repo.listar(token)
                statusMessage = if (created) "Usuario creado correctamente" else "Usuario actualizado correctamente"
                Log.d("BackOfficeUsuarios", "GuardarUsuario: Éxito. Created=$created")
            } catch (e: Exception) {
                errorMessage = "Error al guardar el usuario: ${e.message}"
                statusMessage = errorMessage
                Log.e("BackOfficeUsuarios", "GuardarUsuario: Error", e)
            }
        }
    }

    fun eliminarUsuario(token: String, id: Int) {
        if (token.isBlank()) { statusMessage = "Token de administrador no disponible"; return }
        viewModelScope.launch {
            try {
                Log.d("BackOfficeUsuarios", "EliminarUsuario: id=$id")
                val repo = UsuarioRepository()
                repo.eliminar(token, id)
                _usuarios.value = repo.listar(token)
                if (_usuarioSeleccionado.value?.id == id) nuevoUsuario()
                statusMessage = "Usuario eliminado correctamente"
                Log.d("BackOfficeUsuarios", "EliminarUsuario: Éxito")
            } catch (e: Exception) {
                errorMessage = "Error al eliminar el usuario: ${e.message}"
                statusMessage = errorMessage
                Log.e("BackOfficeUsuarios", "EliminarUsuario: Error", e)
            }
        }
    }
}
