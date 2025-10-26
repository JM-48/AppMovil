package com.example.catalogoproductos.database

import androidx.room.*
import com.example.catalogoproductos.model.Direccion
import kotlinx.coroutines.flow.Flow

@Dao
interface DireccionDao {
    @Query("SELECT * FROM direcciones WHERE emailUsuario = :email")
    fun getDireccionesByUsuario(email: String): Flow<List<Direccion>>

    @Query("SELECT * FROM direcciones WHERE emailUsuario = :email AND esDefault = 1 LIMIT 1")
    fun getDireccionDefaultByUsuario(email: String): Flow<Direccion?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDireccion(direccion: Direccion): Long

    @Update
    suspend fun updateDireccion(direccion: Direccion)

    @Delete
    suspend fun deleteDireccion(direccion: Direccion)
}