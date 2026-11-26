package com.example.contapyme.data.api

import com.example.contapyme.data.model.ClienteDetalleResponse
import com.example.contapyme.data.model.ClienteHistorialResponse
import com.example.contapyme.data.model.ClienteMutationResponse
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.model.ClienteStatusRequest
import com.example.contapyme.data.model.ClientesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientesApi {
    @GET("clientes")
    suspend fun getClientes(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("estatus") estatus: Int? = null,
        @Query("ciudad") ciudad: String? = null,
        @Query("estado") estado: String? = null,
        @Query("colonia") colonia: String? = null,
        @Query("adeudo") adeudo: String? = null
    ): Response<ClientesResponse>

    @GET("clientes/{id}/historial")
    suspend fun getHistorial(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<ClienteHistorialResponse>

    @GET("clientes/{id}")
    suspend fun getCliente(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<ClienteDetalleResponse>

    @POST("clientes")
    suspend fun crearCliente(
        @Header("Authorization") authorization: String,
        @Body request: ClienteRequest
    ): Response<ClienteMutationResponse>

    @PUT("clientes/{id}")
    suspend fun actualizarCliente(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: ClienteRequest
    ): Response<ClienteMutationResponse>

    @PATCH("clientes/{id}/status")
    suspend fun cambiarEstatus(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: ClienteStatusRequest
    ): Response<ClienteMutationResponse>
}
