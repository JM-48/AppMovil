package com.example.catalogoproductos.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.catalogoproductos.model.FakeDatabase
import com.example.catalogoproductos.model.Usuario
import com.example.catalogoproductos.model.Administrador

class AuthViewModel : ViewModel() {

    var mensaje = mutableStateOf("")
    var usuarioActual = mutableStateOf<String?>(null)
    var esAdministrador = mutableStateOf(false)

    init {
        // Crear un administrador por defecto si no existe
        if (!FakeDatabase.existeAdministrador("admin@catalogo.com")) {
            FakeDatabase.registrarAdministrador(
                Administrador(
                    email = "admin@catalogo.com",
                    password = "admin123",
                    nombre = "Administrador"
                )
            )
        }
    }

    fun registrar(nombre: String, email: String, password: String) {
        val nuevo = Usuario(nombre, email, password)
        if (FakeDatabase.registrar(nuevo)) {
            mensaje.value = "Registro exitoso"
        } else {
            mensaje.value = "El usuario ya existe"
        }
    }

    fun login(email: String, password: String): Boolean {
        // Primero verificamos si es un administrador
        if (FakeDatabase.loginAdministrador(email, password)) {
            usuarioActual.value = email
            esAdministrador.value = true
            mensaje.value = "Inicio de sesión como administrador"
            return true
        }
        
        // Si no es administrador, verificamos si es un usuario normal
        return if (FakeDatabase.login(email, password)) {
            usuarioActual.value = email
            esAdministrador.value = false
            mensaje.value = "Inicio de sesión exitoso"
            true
        } else {
            mensaje.value = "Credenciales inválidas"
            false
        }
    }

    fun logout() {
        usuarioActual.value = null
        esAdministrador.value = false
        mensaje.value = "Sesión cerrada"
    }
}