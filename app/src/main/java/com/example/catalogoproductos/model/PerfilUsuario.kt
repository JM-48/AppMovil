package com.example.catalogoproductos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfiles")
data class PerfilUsuario(
    @PrimaryKey
    val email: String,
    var nombre: String,
    var apellido: String = "",
    var telefono: String = "",
    var direccion: String = "",
    var ciudad: String = "",
    var codigoPostal: String = ""
)