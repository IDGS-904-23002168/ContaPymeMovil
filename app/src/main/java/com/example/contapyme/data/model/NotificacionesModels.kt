package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa una notificación obtenida desde el backend.
 */
data class NotificacionDto(
    val id: String = "",
    val categoria: String = "",
    val tipo: String = "",
    val prioridad: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val referencia: String? = null,

    @SerializedName("id_referencia")
    val idReferencia: Int = 0,

    @SerializedName("fecha_vencimiento")
    val fechaVencimiento: String? = null,

    @SerializedName("saldo_pendiente")
    val saldoPendiente: Double? = null
)

/**
 * Contadores de notificaciones por categoría y prioridad.
 */
data class ResumenNotificacionesDto(
    val inventario: Int = 0,
    val cxc: Int = 0,
    val cxp: Int = 0,
    val alta: Int = 0,
    val media: Int = 0
)

/**
 * Información general de las notificaciones autorizadas.
 */
data class NotificacionesDataDto(
    val total: Int = 0,
    val resumen: ResumenNotificacionesDto =
        ResumenNotificacionesDto(),
    val notificaciones: List<NotificacionDto> =
        emptyList()
)

/**
 * Respuesta de GET /api/notificaciones.
 */
data class NotificacionesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: NotificacionesDataDto? = null
)
