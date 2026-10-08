package com.nexodevs.smartpantry.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/status")
    suspend fun getStatus(): ApiStatusResponse

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @GET("api/Auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserProfileResponse

    @GET("api/Inventario")
    suspend fun getInventario(
        @Header("Authorization") authorization: String
    ): List<InventarioResponse>

    @GET("api/Categorias")
    suspend fun getCategorias(
        @Header("Authorization") authorization: String
    ): List<CategoriaResponse>

    @POST("api/Categorias")
    suspend fun crearCategoria(
        @Header("Authorization") authorization: String,
        @Body request: CreateCategoriaRequest
    ): CategoriaResponse

    @GET("api/Productos")
    suspend fun getProductos(
        @Header("Authorization") authorization: String
    ): List<ProductoResponse>

    @POST("api/Productos")
    suspend fun crearProducto(
        @Header("Authorization") authorization: String,
        @Body request: CreateProductoRequest
    ): ProductoResponse

    @POST("api/Inventario")
    suspend fun crearInventario(
        @Header("Authorization") authorization: String,
        @Body request: CreateInventarioRequest
    ): InventarioResponse

    @GET("api/ListasCompras")
    suspend fun getListasCompras(
        @Header("Authorization") authorization: String
    ): List<ListaCompraResponse>

    @GET("api/ListasCompras/{id}")
    suspend fun getListaCompraById(
        @Header("Authorization") authorization: String,
        @Path("id") idLista: Int
    ): ListaCompraResponse

    @POST("api/ListasCompras")
    suspend fun crearListaCompra(
        @Header("Authorization") authorization: String,
        @Body request: CreateListaCompraRequest
    ): ListaCompraResponse

    @PATCH("api/ListasCompras/{id}/completar")
    suspend fun completarListaCompra(
        @Header("Authorization") authorization: String,
        @Path("id") idLista: Int
    ): Response<Unit>

    @GET("api/DetallesListaCompras")
    suspend fun getDetallesListaCompras(
        @Header("Authorization") authorization: String,
        @Query("idLista") idLista: Int? = null
    ): List<DetalleListaResponse>

    @POST("api/DetallesListaCompras")
    suspend fun crearDetalleListaCompra(
        @Header("Authorization") authorization: String,
        @Body request: CreateDetalleListaRequest
    ): DetalleListaResponse

    @PATCH("api/DetallesListaCompras/{id}/toggle-comprado")
    suspend fun toggleCompradoDetalle(
        @Header("Authorization") authorization: String,
        @Path("id") idDetalle: Int
    ): Response<Unit>
}