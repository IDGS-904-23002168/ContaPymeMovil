package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.ApiErrorParser
import com.example.contapyme.data.api.ComprasApi
import com.example.contapyme.data.local.SessionManager
import com.example.contapyme.data.model.CompraDto
import com.example.contapyme.data.model.ComprasDataDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Consulta de compras según /api/compras.
 * Intencionalmente NO expone escritura: no hay POST/PUT/PATCH/DELETE.
 */
class ComprasRepository(private val context: Context) {
    private val sessionManager = SessionManager(context.applicationContext)
    private fun api(): ComprasApi = ApiClient.getComprasApi(context.applicationContext)

    private suspend fun authHeader(): String {
        val token = sessionManager.token.first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Sesión no iniciada. Por favor, ingresa a tu cuenta.")
        }
        return "Bearer $token"
    }

    suspend fun getCompras(
        query: String? = null,
        estatus: String? = null,
        condicionesPago: String? = null,
        desde: String? = null,
        hasta: String? = null,
        limite: Int = 50,
        pagina: Int = 1
    ): Result<ComprasDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = api().getCompras(
                authorization = authHeader(),
                query = query?.takeIf { it.isNotBlank() },
                estatus = estatus,
                condicionesPago = condicionesPago,
                desde = desde,
                hasta = hasta,
                limite = limite,
                pagina = pagina
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: ComprasDataDto())
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar las compras.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompra(id: String): Result<CompraDto?> = withContext(Dispatchers.IO) {
        try {
            val response = api().getCompra(authHeader(), id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar la compra.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
