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

    @Query("UPDATE direcciones SET esDefault = 0 WHERE emailUsuario = :email")
    suspend fun clearDefaultByUsuario(email: String)

    @Query("SELECT * FROM direcciones WHERE emailUsuario = :email AND esDefault = 1 LIMIT 1")
    suspend fun getDireccionDefaultOnceByUsuario(email: String): Direccion?

    @Transaction
    suspend fun insertDireccionAsDefault(email: String, direccion: Direccion): Long {
        clearDefaultByUsuario(email)
        return insertDireccion(direccion)
    }
}