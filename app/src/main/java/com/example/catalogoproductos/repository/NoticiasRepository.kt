package com.example.catalogoproductos.repository

import com.example.catalogoproductos.model.GNewsResponse
import com.example.catalogoproductos.model.Noticia
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class NoticiasRepository {
    suspend fun buscar(
        query: String,
        lang: String = "es",
        max: Int = 10,
        apiKey: String
    ): List<Noticia> {
        require(apiKey.isNotBlank()) { "API key requerida" }
        return withContext(Dispatchers.IO) {
            val q = URLEncoder.encode(query, "UTF-8")
            val urlStr = "https://gnews.io/api/v4/search?q=$q&lang=$lang&max=$max&apikey=$apiKey"
            val connection = (URL(urlStr).openConnection() as HttpURLConnection)
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val json = stream.bufferedReader().use { it.readText() }
                if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
                val resp = Gson().fromJson(json, GNewsResponse::class.java)
                resp.articles ?: emptyList()
            } finally {
                connection.disconnect()
            }
        }
    }
}

