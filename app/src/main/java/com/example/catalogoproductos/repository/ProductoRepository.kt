package com.example.catalogoproductos.repository

import android.content.Context
import com.example.catalogoproductos.model.Producto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.DataOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ProductoRepository {

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
        urlBase: String = "https://apitest-1-95ny.onrender.com/productos"
    ): Producto {
        return withContext(Dispatchers.IO) {
            val url = URL(urlBase)
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val body = mapOf(
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "precio" to producto.precio.toDouble(),
                "tipo" to producto.tipo,
                "imagenUrl" to producto.imagen,
                "stock" to producto.stock
            )
            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { writer ->
                writer.write(jsonBody)
            }

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
                imagen = api.imagen,
                stock = api.stock,
                tipo = api.tipo
            )
        }
    }

    private data class ProductoApi(
        val id: Int,
        val nombre: String,
        val descripcion: String?,
        val precio: Double,
        val imagen: String?,
        val stock: Int = 0,
        val tipo: String?
    )
    private data class UploadResponse(val url: String)

    suspend fun obtenerProductosDesdeApi(url: String = "https://apitest-1-95ny.onrender.com/productos"): List<Producto> {
        return withContext(Dispatchers.IO) {
            val connection = (URL(url).openConnection() as HttpURLConnection)
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val json = stream.bufferedReader().use { it.readText() }
                if (code !in 200..299) throw RuntimeException("HTTP $code")
                val type = object : TypeToken<List<ProductoApi>>() {}.type
                val apiList = Gson().fromJson<List<ProductoApi>>(json, type) ?: emptyList()
                apiList.map {
                    Producto(
                        id = it.id,
                        nombre = it.nombre,
                        descripcion = it.descripcion,
                        precio = it.precio.toInt(),
                        imagen = it.imagen,
                        stock = it.stock,
                        tipo = it.tipo
                    )
                }
            } finally {
                connection.disconnect()
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
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

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
        urlBase: String = "https://apitest-1-95ny.onrender.com/productos"
    ): Producto {
        return withContext(Dispatchers.IO) {
            val url = URL(if (categoriaId != null) "$urlBase/$id?categoriaId=$categoriaId" else "$urlBase/$id")
            val connection = (url.openConnection() as HttpURLConnection)
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val body = mapOf(
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "precio" to producto.precio.toDouble(),
                "imagenUrl" to producto.imagen,
                "stock" to producto.stock
            )
            val jsonBody = Gson().toJson(body)
            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { writer ->
                writer.write(jsonBody)
            }

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
                imagen = api.imagen,
                stock = api.stock,
                tipo = api.tipo
            )
        }
    }

    suspend fun eliminarProducto(id: Int, urlBase: String = "https://apitest-1-95ny.onrender.com/productos") {
        withContext(Dispatchers.IO) {
            val connection = (URL("$urlBase/$id").openConnection() as HttpURLConnection)
            try {
                connection.requestMethod = "DELETE"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val code = connection.responseCode
                if (code !in 200..299) {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    throw RuntimeException("HTTP $code: $error")
                }
            } finally {
                connection.disconnect()
            }
        }
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
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

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
