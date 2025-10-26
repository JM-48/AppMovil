package com.example.catalogoproductos.repository

import com.example.catalogoproductos.database.PerfilUsuarioDao
import com.example.catalogoproductos.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow

class PerfilUsuarioRepository(private val perfilUsuarioDao: PerfilUsuarioDao) {
    
    fun getPerfilUsuario(email: String): Flow<PerfilUsuario?> {
        return perfilUsuarioDao.getPerfilByEmail(email)
    }
    
    suspend fun guardarPerfil(perfil: PerfilUsuario) {
        perfilUsuarioDao.insertPerfil(perfil)
    }
    
    suspend fun actualizarPerfil(perfil: PerfilUsuario) {
        perfilUsuarioDao.updatePerfil(perfil)
    }
}