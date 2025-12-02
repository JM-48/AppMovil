package com.example.catalogoproductos.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository {
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val token: String?, val role: String?, val email: String?)
    data class RegisterRequest(
        val email: String,
        val password: String,
        val nombre: String?,
        val apellido: String?,
        val telefono: String?,
        val direccion: String?,
        val ciudad: String?,
        val codigoPostal: String?,
        val role: String?
    )
    data class MeResponse(val email: String?, val role: String?)

    suspend fun login(email: String, password: String, urlBase: String = "https://apitest-1-95ny.onrender.com/auth/login"): LoginResponse {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val body = LoginRequest(email, password)
            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { it.write(jsonBody) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            try {
                Gson().fromJson(json, LoginResponse::class.java)
            } catch (_: Exception) {
                // fallback por si la respuesta no coincide exactamente
                val map = Gson().fromJson(json, Map::class.java) as Map<*, *>
                LoginResponse(map["token"] as String?, map["role"] as String?, map["email"] as String?)
            }
        }
    }

    suspend fun me(token: String, urlBase: String = "https://apitest-1-95ny.onrender.com/users/me"): MeResponse {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            try {
                Gson().fromJson(json, MeResponse::class.java)
            } catch (_: Exception) {
                val map = Gson().fromJson(json, Map::class.java) as Map<*, *>
                MeResponse(map["email"] as String?, map["role"] as String?)
            }
        }
    }

    suspend fun register(req: RegisterRequest, urlBase: String = "https://apitest-1-95ny.onrender.com/auth/register"): Boolean {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val jsonBody = Gson().toJson(req)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { it.write(jsonBody) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            true
        }
    }

    suspend fun actualizarMiPerfil(
        token: String,
        body: Map<String, Any?>,
        urlBase: String = "https://apitest-1-95ny.onrender.com/users/me"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "PATCH"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { it.write(jsonBody) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            true
        }
    }
}
