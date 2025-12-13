package com.example.catalogoproductos.repository

import com.example.catalogoproductos.network.AuthService
import com.example.catalogoproductos.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val region: String?,
        val codigoPostal: String?,
        val role: String?
    )
    data class ProfileApi(
        val nombre: String?,
        val apellido: String?,
        val telefono: String?,
        val direccion: String?,
        val region: String?,
        val ciudad: String?,
        val codigoPostal: String?
    )
    data class MeResponse(val email: String?, val role: String?, val profile: ProfileApi?)

    private val service: AuthService = RetrofitClient.backend.create(AuthService::class.java)

    suspend fun login(email: String, password: String): LoginResponse {
        return withContext(Dispatchers.IO) {
            try {
                val res = service.login(AuthService.LoginRequest(email, password))
                AuthRepository.LoginResponse(res.token, res.role, res.email)
            } catch (e: java.net.SocketTimeoutException) {
                throw RuntimeException("Timeout de conexión")
            } catch (e: java.io.IOException) {
                throw RuntimeException("Error de red: ${e.message}")
            } catch (e: Exception) {
                throw RuntimeException(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun me(token: String): MeResponse {
        return withContext(Dispatchers.IO) {
            try {
                val res = service.me("Bearer $token")
                val p = res.profile
                val profile = if (p != null) ProfileApi(p.nombre, p.apellido, p.telefono, p.direccion, p.region, p.ciudad, p.codigoPostal) else null
                AuthRepository.MeResponse(res.email, res.role, profile)
            } catch (e: java.net.SocketTimeoutException) {
                throw RuntimeException("Timeout de conexión")
            } catch (e: java.io.IOException) {
                throw RuntimeException("Error de red: ${e.message}")
            } catch (e: Exception) {
                throw RuntimeException(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun register(req: RegisterRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf(
                    "email" to req.email,
                    "password" to req.password,
                    "nombre" to req.nombre,
                    "apellido" to req.apellido,
                    "telefono" to req.telefono,
                    "direccion" to req.direccion,
                    "ciudad" to req.ciudad,
                    "region" to req.region,
                    "codigoPostal" to req.codigoPostal,
                    "role" to req.role
                )
                val response = service.register(body)
                if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}: ${response.errorBody()?.string()}")
                true
            } catch (e: java.net.SocketTimeoutException) {
                throw RuntimeException("Timeout de conexión")
            } catch (e: java.io.IOException) {
                throw RuntimeException("Error de red: ${e.message}")
            } catch (e: Exception) {
                throw RuntimeException(e.message ?: "Error desconocido")
            }
        }
    }

    suspend fun actualizarMiPerfil(
        token: String,
        body: Map<String, Any?>
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.updateMe("Bearer $token", body)
                if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}: ${response.errorBody()?.string()}")
                true
            } catch (e: java.net.SocketTimeoutException) {
                throw RuntimeException("Timeout de conexión")
            } catch (e: java.io.IOException) {
                throw RuntimeException("Error de red: ${e.message}")
            } catch (e: Exception) {
                throw RuntimeException(e.message ?: "Error desconocido")
            }
        }
    }
}
