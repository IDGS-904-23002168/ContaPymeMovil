package com.example.contapyme.data.api

import com.example.contapyme.data.model.ClienteOpcionesResponse
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.model.ClienteResponse
import com.example.contapyme.data.model.ClienteStatsResponse
import com.example.contapyme.data.model.ClienteStatusRequest
import com.example.contapyme.data.model.ClienteStatusResponse
import com.example.contapyme.data.model.ClientesListResponse
import com.example.contapyme.data.model.HistorialResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClienteApi {

    // Los parámetros en null no se mandan en la URL.
    @GET("clientes")
    suspend fun getClientes(
        @Query("q") q: String?,
        @Query("estatus") estatus: String?,   // "1" activos, "0" inactivos
        @Query("ciudad") ciudad: String?,
        @Query("estado") estado: String?,
        @Query("colonia") colonia: String?,
        @Query("adeudo") adeudo: String?      // "con" | "sin"
    ): Response<ClientesListResponse>

    @GET("clientes/stats")
    suspend fun getStats(): Response<ClienteStatsResponse>

    @GET("clientes/opciones")
    suspend fun getOpciones(): Response<ClienteOpcionesResponse>

    @GET("clientes/{id}")
    suspend fun getCliente(@Path("id") id: String): Response<ClienteResponse>

    @GET("clientes/{id}/historial")
    suspend fun getHistorial(@Path("id") id: String): Response<HistorialResponse>

    @POST("clientes")
    suspend fun crearCliente(@Body request: ClienteRequest): Response<ClienteResponse>

    @PUT("clientes/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: String,
        @Body request: ClienteRequest
    ): Response<ClienteResponse>

    @PATCH("clientes/{id}/status")
    suspend fun cambiarEstatus(
        @Path("id") id: String,
        @Body request: ClienteStatusRequest
    ): Response<ClienteStatusResponse>
}
