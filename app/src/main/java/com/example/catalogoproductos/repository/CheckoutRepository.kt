package com.example.catalogoproductos.repository

import com.example.catalogoproductos.network.CheckoutService
import com.example.catalogoproductos.network.CheckoutRequest
import com.example.catalogoproductos.network.ConfirmRequest
import com.example.catalogoproductos.network.OrdenDTO
import com.example.catalogoproductos.network.CompraDTO

class CheckoutRepository(private val checkoutService: CheckoutService) {
    suspend fun checkout(token: String, request: CheckoutRequest): OrdenDTO {
        return checkoutService.checkout("Bearer $token", request)
    }

    suspend fun confirm(token: String, ordenId: Int, referenciaPago: String): CompraDTO {
        return checkoutService.confirm("Bearer $token", ordenId, ConfirmRequest(referenciaPago))
    }
}
