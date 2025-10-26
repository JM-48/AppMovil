package com.example.catalogoproductos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_carrito")
data class ItemCarrito(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productoId: Int,
    val nombre: String,
    val precio: Int,
    val imagen: String,
    var cantidad: Int = 1,
    val emailUsuario: String
)