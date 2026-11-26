package com.example.contapyme.data.api

import com.example.contapyme.data.model.CompraDetalleResponse
import com.example.contapyme.data.model.ComprasResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/** Solo consultas: la app no registra, cancela ni devuelve compras. */
interface ComprasApi {
    @GET("compras")
    suspend fun getCompras(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("estatus") estatus: String? = null,
        @Query("condiciones_pago") condicionesPago: String? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("id_proveedor") idProveedor: Int? = null,
        @Query("limite") limite: Int = 50,
        @Query("pagina") pagina: Int = 1
    ): Response<ComprasResponse>

    @GET("compras/{id}")
    suspend fun getCompra(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<CompraDetalleResponse>
}
