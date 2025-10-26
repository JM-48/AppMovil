package com.example.catalogoproductos.database

import androidx.room.*
import com.example.catalogoproductos.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilUsuarioDao {
    @Query("SELECT * FROM perfiles WHERE email = :email")
    fun getPerfilByEmail(email: String): Flow<PerfilUsuario?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfil(perfil: PerfilUsuario)

    @Update
    suspend fun updatePerfil(perfil: PerfilUsuario)

    @Delete
    suspend fun deletePerfil(perfil: PerfilUsuario)
}