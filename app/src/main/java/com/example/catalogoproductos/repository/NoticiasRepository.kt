package com.example.catalogoproductos.repository

import com.example.catalogoproductos.model.Noticia
import com.example.catalogoproductos.network.NoticiasService
import com.example.catalogoproductos.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoticiasRepository {
    private val service: NoticiasService = RetrofitClient.gnews.create(NoticiasService::class.java)

    suspend fun buscar(
        query: String,
        lang: String = "es",
        max: Int = 10,
        apiKey: String
    ): List<Noticia> {
        require(apiKey.isNotBlank()) { "API key requerida" }
        return withContext(Dispatchers.IO) {
            try {
                val resp = service.search(query = query, lang = lang, max = max, apiKey = apiKey)
                resp.articles ?: emptyList()
            } catch (e: java.net.SocketTimeoutException) {
                emptyList()
            } catch (e: java.io.IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
