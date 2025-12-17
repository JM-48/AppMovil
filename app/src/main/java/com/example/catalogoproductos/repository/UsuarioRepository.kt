package com.example.catalogoproductos.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class UsuarioRepository {
    data class UsuarioDto(
        val id: Int?,
        val email: String?,
        val nombre: String?,
        val apellido: String?,
        val telefono: String?,
        val direccion: String?,
        val ciudad: String?,
        val codigoPostal: String?,
        val role: String?
    )

    suspend fun listar(token: String, urlBase: String = "https://apitest-1-95ny.onrender.com/api/v1/users"): List<UsuarioDto> {
        return withContext(Dispatchers.IO) {
            val connection = (URL(urlBase).openConnection() as HttpURLConnection)
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 60000
                connection.readTimeout = 60000
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val json = stream.bufferedReader().use { it.readText() }
                if (code !in 200..299) return@withContext emptyList()
                try {
                    fun mapToDto(m: Map<String, Any?>): UsuarioDto {
                        @Suppress("UNCHECKED_CAST")
                        val p = (m["profile"] as? Map<*, *>) as? Map<String, Any?>
                        return UsuarioDto(
                            id = (m["id"] as? Number)?.toInt(),
                            email = m["email"] as? String,
                            nombre = (m["nombre"] as? String) ?: (p?.get("nombre") as? String),
                            apellido = (m["apellido"] as? String) ?: (p?.get("apellido") as? String),
                            telefono = (m["telefono"] as? String) ?: (p?.get("telefono") as? String),
                            direccion = (m["direccion"] as? String) ?: (p?.get("direccion") as? String),
                            ciudad = (m["ciudad"] as? String) ?: (p?.get("ciudad") as? String),
                            codigoPostal = (m["codigoPostal"] as? String) ?: (p?.get("codigoPostal") as? String),
                            role = m["role"] as? String
                        )
                    }
                    val gson = Gson()
                    val trimmed = json.trim()
                    if (trimmed.startsWith("[")) {
                        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
                        val list = gson.fromJson<List<Map<String, Any?>>>(json, type) ?: emptyList()
                        list.map { mapToDto(it) }
                    } else {
                        val obj = gson.fromJson(json, Map::class.java) as Map<*, *>
                        val keys = listOf("content", "items", "data", "results", "list", "usuarios", "users")
                        val arrayAny = keys.firstNotNullOfOrNull { k ->
                            val v = obj[k]
                            @Suppress("UNCHECKEDCAST")
                            (v as? List<Map<String, Any?>>)
                        }
                        when {
                            arrayAny != null -> arrayAny.map { mapToDto(it) }
                            obj.isNotEmpty() -> {
                                @Suppress("UNCHECKED_CAST")
                                val m = obj as Map<String, Any?>
                                listOf(mapToDto(m))
                            }
                            else -> emptyList()
                        }
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            } catch (_: java.net.SocketTimeoutException) {
                emptyList()
            } catch (_: java.io.IOException) {
                emptyList()
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun crear(token: String, body: Map<String, Any?>, urlBase: String = "https://apitest-1-95ny.onrender.com/api/v1/users"): UsuarioDto {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { it.write(jsonBody) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            val map = Gson().fromJson(json, Map::class.java) as Map<*, *>
            UsuarioDto(
                id = (map["id"] as? Number)?.toInt(),
                email = map["email"] as? String,
                nombre = map["nombre"] as? String,
                apellido = map["apellido"] as? String,
                telefono = map["telefono"] as? String,
                direccion = map["direccion"] as? String,
                ciudad = map["ciudad"] as? String,
                codigoPostal = map["codigoPostal"] as? String,
                role = map["role"] as? String
            )
        }
    }

    suspend fun actualizar(token: String, id: Int, body: Map<String, Any?>, urlBase: String = "https://apitest-1-95ny.onrender.com/users"): UsuarioDto {
        return withContext(Dispatchers.IO) {
            val url = URL("$urlBase/$id")
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { it.write(jsonBody) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            val map = Gson().fromJson(json, Map::class.java) as Map<*, *>
            UsuarioDto(
                id = (map["id"] as? Number)?.toInt(),
                email = map["email"] as? String,
                nombre = map["nombre"] as? String,
                apellido = map["apellido"] as? String,
                telefono = map["telefono"] as? String,
                direccion = map["direccion"] as? String,
                ciudad = map["ciudad"] as? String,
                codigoPostal = map["codigoPostal"] as? String,
                role = map["role"] as? String
            )
        }
    }

    suspend fun eliminar(token: String, id: Int, urlBase: String = "https://apitest-1-95ny.onrender.com/api/v1/users") {
        withContext(Dispatchers.IO) {
            val url = URL("$urlBase/$id")
            val connection = (url.openConnection() as HttpURLConnection)
            try {
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                val code = connection.responseCode
                if (code !in 200..299) {
                    val err = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    throw RuntimeException("HTTP $code: $err")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}
