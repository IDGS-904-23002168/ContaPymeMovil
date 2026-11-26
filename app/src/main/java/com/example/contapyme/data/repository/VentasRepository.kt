package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.ApiErrorParser
import com.example.contapyme.data.api.VentasApi
import com.example.contapyme.data.local.SessionManager
import com.example.contapyme.data.model.VentaCreada
import com.example.contapyme.data.model.VentaDto
import com.example.contapyme.data.model.VentaFolioResponse
import com.example.contapyme.data.model.VentaListaDto
import com.example.contapyme.data.model.VentaRequest
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Consulta y registro de ventas según /api/ventas. */
class VentasRepository(private val context: Context) {
    private val sessionManager = SessionManager(context.applicationContext)
    private fun api(): VentasApi = ApiClient.getVentasApi(context.applicationContext)

    private suspend fun authHeader(): String {
        val token = sessionManager.token.first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Sesión no iniciada. Por favor, ingresa a tu cuenta.")
        }
        return "Bearer $token"
    }

    suspend fun getVentas(
        query: String? = null,
        estatus: String? = null,
        condicionesPago: String? = null,
        formaPago: String? = null,
        desde: String? = null,
        hasta: String? = null,
        limite: Int = 50,
        pagina: Int = 1
    ): Result<VentaListaDto> = withContext(Dispatchers.IO) {
        try {
            val response = api().getVentas(
                authorization = authHeader(),
                query = query?.takeIf { it.isNotBlank() },
                estatus = estatus,
                condicionesPago = condicionesPago,
                formaPago = formaPago,
                desde = desde,
                hasta = hasta,
                limite = limite,
                pagina = pagina
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: VentaListaDto())
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar las ventas.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVenta(id: String): Result<VentaDto?> = withContext(Dispatchers.IO) {
        try {
            val response = api().getVenta(authHeader(), id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar la venta.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSiguienteFolio(): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val response = api().getSiguienteFolio(authHeader())
            if (response.isSuccessful) {
                Result.success(extraerFolio(response.body()))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun crearVenta(request: VentaRequest): Result<VentaCreada> = withContext(Dispatchers.IO) {
        try {
            val response = api().crearVenta(authHeader(), request)
            val body = response.body()
            if (response.isSuccessful) {
                Result.success(
                    VentaCreada(
                        idVenta = body?.data?.idVenta ?: body?.idVenta ?: 0,
                        codigo = body?.data?.codigo ?: body?.codigo,
                        mensaje = body?.message
                    )
                )
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible registrar la venta.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extraerFolio(body: VentaFolioResponse?): String? {
        if (body == null) return null
        val data: JsonElement? = body.data
        if (data != null && !data.isJsonNull) {
            if (data.isJsonPrimitive) {
                val valor = data.asString
                if (valor.isNotBlank()) return valor
            }
            if (data.isJsonObject) {
                val objeto = data.asJsonObject
                for (clave in listOf("codigo", "folio", "siguiente_folio", "numero", "value")) {
                    val valor = objeto.get(clave)?.takeUnless { it.isJsonNull }?.asString
                    if (!valor.isNullOrBlank()) return valor
                }
            }
        }
        return body.codigo ?: body.folio
    }
}
