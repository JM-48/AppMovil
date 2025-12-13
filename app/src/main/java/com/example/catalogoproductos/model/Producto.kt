package com.example.catalogoproductos.model

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Int,
    val precioOriginal: Int? = null,
    val imagen: String?,
    val stock: Int = 0,
    val tipo: String? = null
)
