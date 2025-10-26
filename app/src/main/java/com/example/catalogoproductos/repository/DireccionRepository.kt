package com.example.catalogoproductos.repository

import com.example.catalogoproductos.database.DireccionDao
import com.example.catalogoproductos.model.Direccion
import kotlinx.coroutines.flow.Flow

class DireccionRepository(private val direccionDao: DireccionDao) {
    
    fun getDirecciones(email: String): Flow<List<Direccion>> {
        return direccionDao.getDireccionesByUsuario(email)
    }
    
    fun getDireccionDefault(email: String): Flow<Direccion?> {
        return direccionDao.getDireccionDefaultByUsuario(email)
    }
    
    suspend fun guardarDireccion(direccion: Direccion): Long {
        return direccionDao.insertDireccion(direccion)
    }
    
    suspend fun actualizarDireccion(direccion: Direccion) {
        direccionDao.updateDireccion(direccion)
    }
    
    suspend fun eliminarDireccion(direccion: Direccion) {
        direccionDao.deleteDireccion(direccion)
    }
}