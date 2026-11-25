package com.example.contapyme.data.api

import com.example.contapyme.data.model.NotificacionesResponse
import retrofit2.Response
import retrofit2.http.GET

interface NotificacionesApi {

    /**
     * Consulta las notificaciones correspondientes
     * al usuario autenticado y su empresa.
     */
    @GET("notificaciones")
    suspend fun getNotificaciones(): Response<NotificacionesResponse>
}
