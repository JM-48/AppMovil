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
        .baseUrl("https://apitest-1-95ny.onrender.com/")
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

interface NoticiasService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("lang") lang: String,
        @Query("max") max: Int,
        @Query("apikey") apiKey: String
    ): com.example.catalogoproductos.model.GNewsResponse
}
