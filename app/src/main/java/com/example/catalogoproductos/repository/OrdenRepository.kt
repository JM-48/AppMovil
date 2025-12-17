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

    suspend fun updateOrder(token: String, id: Int, status: String): OrdenDTO {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val body = mapOf("status" to status)
        return ordenService.updateOrder(bearer, id, body)
    }
}
