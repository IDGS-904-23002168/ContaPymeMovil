package com.example.contapyme.data.api

import com.example.contapyme.data.model.VentaCreadaResponse
import com.example.contapyme.data.model.VentaDetalleResponse
import com.example.contapyme.data.model.VentaFolioResponse
import com.example.contapyme.data.model.VentaRequest
import com.example.contapyme.data.model.VentasResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VentasApi {
    // Las rutas estáticas se declaran antes que /ventas/{id} para evitar capturarlas como ID.
    @GET("ventas/siguiente-folio")
    suspend fun getSiguienteFolio(
        @Header("Authorization") authorization: String
    ): Response<VentaFolioResponse>

    @GET("ventas")
    suspend fun getVentas(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("estatus") estatus: String? = null,
        @Query("condiciones_pago") condicionesPago: String? = null,
        @Query("forma_pago") formaPago: String? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("id_cliente") idCliente: Int? = null,
        @Query("limite") limite: Int = 50,
        @Query("pagina") pagina: Int = 1
    ): Response<VentasResponse>

    @POST("ventas")
    suspend fun crearVenta(
        @Header("Authorization") authorization: String,
        @Body request: VentaRequest
    ): Response<VentaCreadaResponse>

    @GET("ventas/{id}")
    suspend fun getVenta(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<VentaDetalleResponse>
}
