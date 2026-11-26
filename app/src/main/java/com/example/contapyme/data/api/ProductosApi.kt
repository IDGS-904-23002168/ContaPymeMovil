package com.example.contapyme.data.api

import com.example.contapyme.data.model.ProductoDetalleResponse
import com.example.contapyme.data.model.ProductosResponse
import com.example.contapyme.data.model.ProductosStatsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductosApi {
    @GET("productos")
    suspend fun getProductos(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("estatus") estatus: Int? = null
    ): Response<ProductosResponse>

    @GET("productos/stats")
    suspend fun getStats(
        @Header("Authorization") authorization: String
    ): Response<ProductosStatsResponse>

    @GET("productos/{id}")
    suspend fun getProducto(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<ProductoDetalleResponse>
}
