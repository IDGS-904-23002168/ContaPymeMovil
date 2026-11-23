package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.apiCall
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.ClienteOpcionesDto
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.model.ClienteStatsDto
import com.example.contapyme.data.model.ClienteStatusRequest
import com.example.contapyme.data.model.HistorialDto

class ClienteRepository(context: Context) {
    private val appContext = context.applicationContext

    // Se pide en cada llamada para respetar un cambio de URL desde la pantalla de configuración.
    private val api get() = ApiClient.getClienteApi(appContext)

    suspend fun listar(
        q: String?,
        estatus: String?,
        ciudad: String?,
        estado: String?,
        adeudo: String?
    ): Result<List<ClienteDto>> =
        apiCall { api.getClientes(q?.takeIf { it.isNotBlank() }, estatus, ciudad, estado, null, adeudo) }
            .map { it.data }

    suspend fun stats(): Result<ClienteStatsDto> =
        apiCall { api.getStats() }.mapCatching { it.data ?: ClienteStatsDto() }

    suspend fun opciones(): Result<ClienteOpcionesDto> =
        apiCall { api.getOpciones() }.mapCatching { it.data ?: ClienteOpcionesDto() }

    suspend fun obtener(id: String): Result<ClienteDto> =
        apiCall { api.getCliente(id) }.mapCatching { it.data ?: error("Cliente no encontrado") }

    suspend fun historial(id: String): Result<HistorialDto> =
        apiCall { api.getHistorial(id) }.mapCatching { it.data ?: HistorialDto() }

    suspend fun crear(request: ClienteRequest): Result<ClienteDto> =
        apiCall { api.crearCliente(request) }.mapCatching { it.data ?: error("Respuesta vacía del servidor") }

    suspend fun actualizar(id: String, request: ClienteRequest): Result<ClienteDto> =
        apiCall { api.actualizarCliente(id, request) }.mapCatching { it.data ?: error("Respuesta vacía del servidor") }

    /** Devuelve el mensaje del servidor ("Cliente activado exitosamente.", etc.). */
    suspend fun cambiarEstatus(id: String, activo: Boolean): Result<String> =
        apiCall { api.cambiarEstatus(id, ClienteStatusRequest(if (activo) 1 else 0)) }
            .map { it.message ?: "Estatus actualizado" }
}