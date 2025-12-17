package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogoproductos.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var mensaje = mutableStateOf("")
    var usuarioActual = mutableStateOf<String?>(null)
    var esAdministrador = mutableStateOf(false)
    var token = mutableStateOf<String?>(null)
    var role = mutableStateOf<String?>(null)

    private val repo = AuthRepository()

    private fun normalizeRole(input: String?): String {
        val r = input?.uppercase()?.trim()
        return when (r) {
            "ADMIN", "USER_AD", "PROD_AD", "CLIENT", "VENDEDOR" -> r
            else -> "CLIENT"
        }
    }

    fun canAccessBackofficeProductos(): Boolean {
        return when (normalizeRole(role.value)) {
            "ADMIN", "PROD_AD", "VENDEDOR" -> true
            else -> false
        }
    }

    fun canAccessAdminOrdenes(): Boolean {
        return when (normalizeRole(role.value)) {
            "ADMIN", "VENDEDOR" -> true
            else -> false
        }
    }

    fun canAccessBackofficeUsuarios(): Boolean {
        return when (normalizeRole(role.value)) {
            "ADMIN", "USER_AD" -> true
            else -> false
        }
    }

    fun registrar(nombre: String, email: String, password: String) {
        mensaje.value = "Usa el formulario de registro"
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val resp = repo.login(email, password)
                val tk = resp.token
                if (tk.isNullOrBlank()) {
                    mensaje.value = "Login sin token"
                    return@launch
                }
                token.value = tk
                // Obtener perfil con rol
                val me = repo.me(tk)
                usuarioActual.value = me.email ?: email
                role.value = normalizeRole(me.role ?: resp.role)
                esAdministrador.value = (role.value?.equals("ADMIN", ignoreCase = true) == true)
                mensaje.value = if (esAdministrador.value) "Inicio de sesión como administrador" else "Inicio de sesión exitoso"
            } catch (e: Exception) {
                mensaje.value = "Credenciales inválidas: ${e.message}".replace("\n", " ")
            }
        }
    }

    fun logout() {
        usuarioActual.value = null
        esAdministrador.value = false
        token.value = null
        role.value = "CLIENT"
        mensaje.value = "Sesión cerrada"
    }
}
