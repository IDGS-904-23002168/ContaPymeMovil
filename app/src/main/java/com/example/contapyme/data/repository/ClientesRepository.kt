package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.ApiErrorParser
import com.example.contapyme.data.api.ClientesApi
import com.example.contapyme.data.local.SessionManager
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.ClienteHistorialDto
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.model.ClienteStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** CRUD de clientes: GET, POST, PUT y PATCH de estatus según /api/clientes. */
class ClientesRepository(private val context: Context) {
    private val sessionManager = SessionManager(context.applicationContext)
    private fun api(): ClientesApi = ApiClient.getClientesApi(context.applicationContext)

    private suspend fun authHeader(): String {
        val token = sessionManager.token.first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Sesión no iniciada. Por favor, ingresa a tu cuenta.")
        }
        return "Bearer $token"
    }

    suspend fun getClientes(
        query: String? = null,
        estatus: Int? = null,
        ciudad: String? = null,
        estado: String? = null,
        adeudo: String? = null
    ): Result<List<ClienteDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api().getClientes(
                authorization = authHeader(),
                query = query?.takeIf { it.isNotBlank() },
                estatus = estatus,
                ciudad = ciudad?.takeIf { it.isNotBlank() },
                estado = estado?.takeIf { it.isNotBlank() },
                adeudo = adeudo?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.data.orEmpty())
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar los clientes.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCliente(id: String): Result<ClienteDto?> = withContext(Dispatchers.IO) {
        try {
            val response = api().getCliente(authHeader(), id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar el cliente.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistorial(id: String): Result<ClienteHistorialDto?> = withContext(Dispatchers.IO) {
        try {
            val response = api().getHistorial(authHeader(), id)
            if (response.isSuccessful) {
                Result.success(response.body()?.historial())
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible consultar el historial del cliente.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearCliente(request: ClienteRequest): Result<ClienteDto?> = withContext(Dispatchers.IO) {
        try {
            val response = api().crearCliente(authHeader(), request)
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.data ?: ClienteDto(idCliente = body?.idCliente ?: 0))
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible registrar el cliente.",
                            response.code()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarCliente(id: String, request: ClienteRequest): Result<ClienteDto?> =
        withContext(Dispatchers.IO) {
            try {
                val response = api().actualizarCliente(authHeader(), id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    Result.success(body?.data ?: ClienteDto(idCliente = body?.idCliente ?: 0))
                } else {
                    Result.failure(
                        Exception(
                            ApiErrorParser.parse(
                                response.errorBody()?.string(),
                                "No fue posible actualizar el cliente.",
                                response.code()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun cambiarEstatus(id: String, estatus: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api().cambiarEstatus(authHeader(), id, ClienteStatusRequest(estatus))
            if (response.isSuccessful) {
                Result.success(response.body()?.success ?: true)
            } else {
                Result.failure(
                    Exception(
                        ApiErrorParser.parse(
                            response.errorBody()?.string(),
                            "No fue posible cambiar el estatus del cliente.",
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
