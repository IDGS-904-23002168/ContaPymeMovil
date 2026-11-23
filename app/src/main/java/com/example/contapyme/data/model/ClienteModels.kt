package com.example.contapyme.data.model

import com.example.contapyme.data.Cliente
import com.google.gson.annotations.SerializedName

data class ClienteDto(
    @SerializedName("id_cliente") val idCliente: Int = 0,
    @SerializedName("nombre_cliente") val nombreCliente: String? = null,
    @SerializedName("apellido_paterno") val apellidoPaterno: String? = null,
    @SerializedName("apellido_materno") val apellidoMaterno: String? = null,
    @SerializedName("nombre_completo") val nombreCompleto: String? = null,
    val calle: String? = null,
    @SerializedName("num_exterior") val numExterior: String? = null,
    val colonia: String? = null,
    @SerializedName("codigo_postal") val codigoPostal: String? = null,
    val ciudad: String? = null,
    val estado: String? = null,
    val telefono: String? = null,
    val correo: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String? = null,
    val estatus: Int = 1,
    // Postgres manda los numeric como texto ("10496.00"); Gson lo convierte solo.
    @SerializedName("saldo_pendiente") val saldoPendiente: Double = 0.0
)

data class ClientesListResponse(
    val success: Boolean = false,
    val data: List<ClienteDto> = emptyList(),
    val message: String? = null
)

data class ClienteResponse(
    val success: Boolean = false,
    val data: ClienteDto? = null,
    val message: String? = null
)

data class ClienteStatsDto(
    val total: Int = 0,
    val activos: Int = 0,
    val inactivos: Int = 0,
    @SerializedName("con_adeudo") val conAdeudo: Int = 0
)

data class ClienteStatsResponse(
    val success: Boolean = false,
    val data: ClienteStatsDto? = null
)

data class ClienteOpcionesDto(
    val ciudades: List<String> = emptyList(),
    val estados: List<String> = emptyList(),
    val colonias: List<String> = emptyList()
)

data class ClienteOpcionesResponse(
    val success: Boolean = false,
    val data: ClienteOpcionesDto? = null
)

/** Cuerpo para POST /api/clientes y PUT /api/clientes/:id */
data class ClienteRequest(
    @SerializedName("nombre_cliente") val nombreCliente: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String? = null,
    val calle: String,
    @SerializedName("num_exterior") val numExterior: String,
    val colonia: String,
    @SerializedName("codigo_postal") val codigoPostal: String,
    val ciudad: String,
    val estado: String,
    val telefono: String,
    val correo: String
)

/** Cuerpo para PATCH /api/clientes/:id/status  (1 = activo, 0 = inactivo) */
data class ClienteStatusRequest(val estatus: Int)

data class ClienteStatusResponse(
    val success: Boolean = false,
    val message: String? = null,
    val estatus: Int = 1
)

// Historial: GET /api/clientes/:id/historial

data class VentaHistorialDto(
    @SerializedName("id_venta") val idVenta: Int = 0,
    val codigo: String? = null,
    val fecha: String? = null,
    val total: Double = 0.0,
    @SerializedName("monto_pagado") val montoPagado: Double = 0.0,
    val estatus: String? = null,
    val productos: String? = null
)

data class CobroHistorialDto(
    @SerializedName("id_cobro") val idCobro: Int = 0,
    val fecha: String? = null,
    val monto: Double = 0.0,
    @SerializedName("forma_pago") val formaPago: String? = null,
    @SerializedName("id_venta") val idVenta: Int? = null,
    @SerializedName("venta_codigo") val ventaCodigo: String? = null
)

data class ResumenHistorialDto(
    @SerializedName("total_ventas") val totalVentas: Double = 0.0,
    @SerializedName("total_cobrado") val totalCobrado: Double = 0.0,
    @SerializedName("saldo_pendiente") val saldoPendiente: Double = 0.0
)

data class HistorialDto(
    val ventas: List<VentaHistorialDto> = emptyList(),
    val cobros: List<CobroHistorialDto> = emptyList(),
    val resumen: ResumenHistorialDto = ResumenHistorialDto()
)

data class HistorialResponse(
    val success: Boolean = false,
    val data: HistorialDto? = null
)

// DTO -> modelo que ya usa tu UI

/** Código visible, igual que en web: 4 -> "C-0004". El backend acepta "4" o "C-0004". */
fun Int.toCodigoCliente(): String = "C-%04d".format(this)

fun ClienteDto.toUi(): Cliente {
    val nombre = (nombreCompleto?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(nombreCliente, apellidoPaterno, apellidoMaterno).joinToString(" ")).trim()

    val iniciales = listOfNotNull(nombreCliente, apellidoPaterno)
        .mapNotNull { it.trim().firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifEmpty { "?" }

    return Cliente(
        id = idCliente.toCodigoCliente(),
        iniciales = iniciales,
        nombre = nombre,
        ciudad = listOfNotNull(ciudad, estado).filter { it.isNotBlank() }.joinToString(", "),
        estado = estado.orEmpty(),
        telefono = telefono.orEmpty(),
        correo = correo.orEmpty(),
        adeudo = saldoPendiente,
        activo = estatus == 1
    )
}
