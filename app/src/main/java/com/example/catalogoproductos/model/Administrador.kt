package com.example.catalogoproductos.model

data class Administrador(
    val email: String,
    val password: String,
    val nombre: String,
    val rol: String = "ADMIN"
)