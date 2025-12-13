package com.example.catalogoproductos.repository

import android.content.Context
import com.example.catalogoproductos.model.Producto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.catalogoproductos.network.ProductoService
import com.example.catalogoproductos.network.ProductoApi
import com.example.catalogoproductos.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.DataOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ProductoRepository {
    private val service: ProductoService = RetrofitClient.backend.create(ProductoService::class.java)

    fun obtenerProductosDesdeAssets(context: Context, filename: String = "productos.json"): List<Producto> {
        return try {
            val json = context.assets.open(filename).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Producto>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun crearProductoJson(
        producto: Producto,
        token: String
    ): Producto {
        return withContext(Dispatchers.IO) {
            val body = mapOf(
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "precio" to producto.precio.toDouble(),
                "tipo" to producto.tipo,
                "imagenUrl" to producto.imagen,
                "stock" to producto.stock
            )
            val api = service.create("Bearer $token", body)
            Producto(
                id = api.id,
                nombre = api.nombre,
                descripcion = api.descripcion,
                precio = api.precio.toInt(),
                imagen = api.imagen,
                stock = api.stock,
                tipo = api.tipo
            )
        }
    }

    private data class UploadResponse(val url: String)

    suspend fun obtenerProductosDesdeApi(): List<Producto> {
        return withContext(Dispatchers.IO) {
            try {
                val apiList = service.list()
                apiList.map {
                    Producto(
                        id = it.id,
                        nombre = it.nombre,
                        descripcion = it.descripcion,
                        precio = it.precio.toInt(),
                        precioOriginal = it.precioOriginal?.toInt(),
                        imagen = it.imagen,
                        stock = it.stock,
                        tipo = it.tipo
                    )
                }
            } catch (e: java.net.SocketTimeoutException) {
                emptyList()
            } catch (e: java.io.IOException) {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun crearProductoMultipart(
        nombre: String,
        descripcion: String?,
        precio: Double,
        tipo: String?,
        stock: Int?,
        imagenFile: File?,
        url: String = "https://apitest-1-95ny.onrender.com/productos"
    ): Producto {
        return withContext(Dispatchers.IO) {
            val boundary = "----AndroidBoundary${System.currentTimeMillis()}"
            val connection = (URL(url).openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000

            val output = DataOutputStream(connection.outputStream)
            fun writeFormField(name: String, value: String) {
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                output.writeBytes(value)
                output.writeBytes("\r\n")
            }
            fun writeFileField(name: String, file: File) {
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"$name\"; filename=\"${file.name}\"\r\n")
                output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                file.inputStream().use { it.copyTo(output) }
                output.writeBytes("\r\n")
            }

            writeFormField("nombre", nombre)
            descripcion?.let { writeFormField("descripcion", it) }
            writeFormField("precio", precio.toString())
            tipo?.let { writeFormField("tipo", it) }
            stock?.let { writeFormField("stock", it.toString()) }
            if (imagenFile != null && imagenFile.exists()) {
                writeFileField("imagen", imagenFile)
            }
            output.writeBytes("--$boundary--\r\n")
            output.flush()
            output.close()

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            val api = Gson().fromJson(json, ProductoApi::class.java)
            Producto(
                id = api.id,
                nombre = api.nombre,
                descripcion = api.descripcion,
                precio = api.precio.toInt(),
                precioOriginal = api.precioOriginal?.toInt(),
                imagen = api.imagen,
                stock = api.stock,
                tipo = api.tipo
            )
        }
    }

    suspend fun actualizarProductoJson(
        id: Int,
        producto: Producto,
        categoriaId: Long? = null,
        token: String
    ): Producto {
        return withContext(Dispatchers.IO) {
            val body = mapOf(
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "precio" to producto.precio.toDouble(),
                "imagenUrl" to producto.imagen,
                "stock" to producto.stock
            )
            val api = service.update("Bearer $token", id, body)
            Producto(
                id = api.id,
                nombre = api.nombre,
                descripcion = api.descripcion,
                precio = api.precio.toInt(),
                imagen = api.imagen,
                stock = api.stock,
                tipo = api.tipo
            )
        }
    }

    suspend fun eliminarProducto(id: Int, token: String) {
        withContext(Dispatchers.IO) { service.delete("Bearer $token", id) }
    }

    suspend fun subirImagen(
        imagenFile: File,
        url: String = "https://apitest-1-95ny.onrender.com/imagenes",
        baseUrlHeader: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            val boundary = "----AndroidBoundary${System.currentTimeMillis()}"
            val connection = (URL(url).openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            if (!baseUrlHeader.isNullOrBlank()) {
                connection.setRequestProperty("X-Base-Url", baseUrlHeader)
            }
            connection.connectTimeout = 60000
            connection.readTimeout = 60000

            val output = DataOutputStream(connection.outputStream)
            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"imagen\"; filename=\"${imagenFile.name}\"\r\n")
            output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            imagenFile.inputStream().use { it.copyTo(output) }
            output.writeBytes("\r\n")
            output.writeBytes("--$boundary--\r\n")
            output.flush()
            output.close()

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val json = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("HTTP $code: $json")
            val resp = Gson().fromJson(json, UploadResponse::class.java)
            resp.url
        }
    }
}
