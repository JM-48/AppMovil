package com.example.catalogoproductos.repository

import com.example.catalogoproductos.network.OrdenDTO
import com.example.catalogoproductos.network.OrdenService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrdenRepository(private val ordenService: OrdenService) {

    fun getMyOrders(token: String): Flow<List<OrdenDTO>> = flow {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        emit(ordenService.getMyOrders(bearer))
    }

    fun getAllOrders(token: String): Flow<List<OrdenDTO>> = flow {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        emit(ordenService.getAllOrders(bearer))
    }

    suspend fun updateOrder(
        token: String, 
        id: Int, 
        status: String,
        destinatario: String? = null,
        direccion: String? = null,
        region: String? = null,
        ciudad: String? = null,
        codigoPostal: String? = null
    ): OrdenDTO {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val body = mutableMapOf<String, Any>("status" to status)
        destinatario?.let { body["destinatario"] = it }
        direccion?.let { body["direccion"] = it }
        region?.let { body["region"] = it }
        ciudad?.let { body["ciudad"] = it }
        codigoPostal?.let { body["codigoPostal"] = it }
        
        return ordenService.updateOrder(bearer, id, body)
    }
}
