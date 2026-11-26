
package com.example.contapyme.data.api

import com.example.contapyme.data.model.CategoriasGastosResponse
import com.example.contapyme.data.model.GastoRequest
import com.example.contapyme.data.model.GastoResponse
import com.example.contapyme.data.model.GastosListResponse
import com.example.contapyme.data.model.ResumenGastosResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GastoApi {

    @GET("gastos")
    suspend fun getGastos(
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null,
        @Query("id_categoria") idCategoria: Int? = null,
        @Query("concepto") concepto: String? = null
    ): Response<GastosListResponse>

    @GET("gastos/categorias")
    suspend fun getCategorias(): Response<CategoriasGastosResponse>

    @GET("gastos/resumen")
    suspend fun getResumen(): Response<ResumenGastosResponse>

    @GET("gastos/{id}")
    suspend fun getGasto(
        @Path("id") id: Int
    ): Response<GastoResponse>

    @POST("gastos")
    suspend fun crearGasto(
        @Body request: GastoRequest
    ): Response<GastoResponse>

    @PUT("gastos/{id}")
    suspend fun actualizarGasto(
        @Path("id") id: Int,
        @Body request: GastoRequest
    ): Response<GastoResponse>

    @PATCH("gastos/{id}/cancelar")
    suspend fun cancelarGasto(
        @Path("id") id: Int
    ): Response<GastoResponse>
}
