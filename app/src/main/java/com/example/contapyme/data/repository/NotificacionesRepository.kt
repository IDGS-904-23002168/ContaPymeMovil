package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.apiCall
import com.example.contapyme.data.model.NotificacionesDataDto

class NotificacionesRepository(context: Context) {

    private val appContext = context.applicationContext

    // Obtener la API en cada consulta para respetar
    // los cambios de URL del servidor.
    private val api
        get() = ApiClient.getNotificacionesApi(appContext)

    /**
     * Obtiene las notificaciones autorizadas
     * para el usuario y la empresa de la sesión activa.
     */
    suspend fun obtenerNotificaciones(): Result<NotificacionesDataDto> =
        apiCall {
            api.getNotificaciones()
        }.mapCatching { response ->

            if (!response.success) {
                error(
                    response.message
                        ?: "No se pudieron obtener las notificaciones"
                )
            }

            response.data
                ?: error("Respuesta vacía del servidor")
        }
}
