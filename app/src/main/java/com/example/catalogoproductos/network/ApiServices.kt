package com.example.catalogoproductos.network

import com.google.gson.GsonBuilder
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()
    private val gson = GsonBuilder().create()

    val backend: Retrofit = Retrofit.Builder()
        .baseUrl("https://apitest-1-95ny.onrender.com/api/v1/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val gnews: Retrofit = Retrofit.Builder()
        .baseUrl("https://gnews.io/api/v4/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

interface ProductoService {
    @GET("productos")
    suspend fun list(): List<ProductoApi>

    @POST("productos")
    suspend fun create(@Header("Authorization") bearer: String, @Body body: Map<String, @JvmSuppressWildcards Any?>): ProductoApi

    @PUT("productos/{id}")
    suspend fun update(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>): ProductoApi

    @DELETE("productos/{id}")
    suspend fun delete(@Header("Authorization") bearer: String, @Path("id") id: Int)

    @GET("productos/{id}/precio")
    suspend fun detallePrecio(@Path("id") id: Int): PrecioDetalleApi
}

data class ProductoApi(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val precioOriginal: Double?,
    val imagen: String?,
    val stock: Int = 0,
    val tipo: String?
)

data class PrecioDetalleApi(
    val precioOriginal: Double,
    val precioConIva: Double,
    val ivaPorcentaje: Double,
    val ivaMonto: Double
)

interface UploadService {
    @Multipart
    @POST("imagenes")
    suspend fun upload(@Part("imagen") imagen: okhttp3.MultipartBody.Part): UploadResponse
}

data class UploadResponse(val url: String)

interface AuthService {
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val token: String?, val role: String?, val email: String?)
    data class ProfileApi(
        val nombre: String?,
        val apellido: String?,
        val telefono: String?,
        val direccion: String?,
        val region: String?,
        val ciudad: String?,
        val codigoPostal: String?
    )
    data class MeResponse(
        val email: String?,
        val role: String?,
        val profile: ProfileApi?,
        val region: String?,
        val ciudad: String?
    )

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("users/me")
    suspend fun me(@Header("Authorization") bearer: String): MeResponse

    @POST("auth/register")
    suspend fun register(@Body body: Map<String, @JvmSuppressWildcards Any?>): retrofit2.Response<Unit>

    @PATCH("users/me")
    suspend fun updateMe(
        @Header("Authorization") bearer: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): retrofit2.Response<Unit>
}

interface CheckoutService {
    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") bearer: String,
        @Body body: CheckoutRequest
    ): OrdenDTO

    @POST("checkout/{ordenId}/confirm")
    suspend fun confirm(
        @Header("Authorization") bearer: String,
        @Path("ordenId") ordenId: Int,
        @Body body: ConfirmRequest
    ): CompraDTO
}

data class CheckoutRequest(
    val items: List<DetalleOrdenRequest>,
    val total: Double,
    val metodoEnvio: String, // "domicilio" | "retiro"
    val metodoPago: String, // "local" | "tarjeta"
    val destinatario: String,
    val direccion: String,
    val region: String,
    val ciudad: String,
    val codigoPostal: String
)

data class DetalleOrdenRequest(
    val productoId: String, // API doc says "1" (string) in example, but ID is Int usually. Doc example: "productoId": "1". Let's use String to be safe or Int if we know.
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int
)

data class ConfirmRequest(
    val referenciaPago: String
)

data class OrdenDTO(
    val id: Int,
    val status: String,
    val total: Double,
    val items: List<DetalleOrdenDTO>,
    val createdAt: String?,
    val fechaPedido: String?,
    val destinatario: String?,
    val direccion: String?,
    val region: String?,
    val ciudad: String?,
    val codigoPostal: String?
)

data class DetalleOrdenDTO(
    val productoId: Int,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val total: Double,
    val imagen: String?
)

data class CompraDTO(
    val id: Int,
    val ordenId: Int,
    val estado: String,
    val monto: Double,
    val referenciaPago: String,
    val fechaPago: String?
)

interface NoticiasService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("lang") lang: String,
        @Query("max") max: Int,
        @Query("apikey") apiKey: String
    ): com.example.catalogoproductos.model.GNewsResponse
}

interface CartService {
    @GET("cart")
    suspend fun getCart(@Header("Authorization") bearer: String): OrdenDTO
    
    @POST("cart/items")
    suspend fun addItem(@Header("Authorization") bearer: String, @Body body: Map<String, Any>): OrdenDTO
    
    @PUT("cart/items/{id}")
    suspend fun updateItem(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body body: Map<String, Int>): OrdenDTO
    
    @DELETE("cart/items/{id}")
    suspend fun deleteItem(@Header("Authorization") bearer: String, @Path("id") id: Int): OrdenDTO
}

interface OrdenService {
    @GET("orders")
    suspend fun getMyOrders(@Header("Authorization") bearer: String): List<OrdenDTO>

    @GET("orders/admin")
    suspend fun getAllOrders(@Header("Authorization") bearer: String): List<OrdenDTO>

    @PATCH("orders/{id}")
    suspend fun updateOrder(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): OrdenDTO
}
