package com.example.catalogoproductos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "direcciones")
data class Direccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val emailUsuario: String,
    var calle: String,
    var numero: String,
    var ciudad: String,
    var provincia: String,
    var codigoPostal: String,
    var telefono: String,
    var esDefault: Boolean = false
)