package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

/**
 * Contratos de /api/clientes. Se aceptan alias porque el backend y el frontend
 * histórico usan nombres ligeramente distintos en algunas respuestas.
 */
data class ClienteDto(
    @SerializedName("id_cliente", alternate = ["id", "idCliente"])
    val idCliente: Int = 0,

    @SerializedName("codigo", alternate = ["codigo_cliente"])
    val codigo: String? = null,

    @SerializedName("nombre_cliente", alternate = ["nombre", "nombreCompleto", "nombre_completo"])
    val nombreCliente: String? = null,

    @SerializedName("apellido_paterno", alternate = ["apellidoPaterno"])
    val apellidoPaterno: String? = null,

    @SerializedName("apellido_materno", alternate = ["apellidoMaterno"])
    val apellidoMaterno: String? = null,

    @SerializedName("calle")
    val calle: String? = null,

    @SerializedName("num_exterior", alternate = ["numero_exterior", "numero"])
    val numExterior: String? = null,

    @SerializedName("colonia")
    val colonia: String? = null,

    @SerializedName("codigo_postal", alternate = ["cp"])
    val codigoPostal: String? = null,

    @SerializedName("ciudad")
    val ciudad: String? = null,

    @SerializedName("estado")
    val estado: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("correo", alternate = ["email"])
    val correo: String? = null,

    @SerializedName("estatus", alternate = ["activo"])
    val estatus: Int = 1,

    @SerializedName("saldo_pendiente", alternate = ["adeudo", "saldo"])
    val saldoPendiente: Double = 0.0,

    @SerializedName("fecha_registro", alternate = ["fecha_creacion", "created_at"])
    val fechaRegistro: String? = null
) {
    val displayId: String
        get() = codigo?.takeIf { it.isNotBlank() } ?: "C-${idCliente.toString().padStart(4, '0')}"

    val nombreCompleto: String
        get() = listOfNotNull(nombreCliente, apellidoPaterno, apellidoMaterno)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    val iniciales: String
        get() {
            val partes = nombreCompleto.split(" ").filter { it.isNotBlank() }
            return when {
                partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
                partes.isNotEmpty() -> partes[0].take(2).uppercase()
                else -> "C"
            }
        }

    val activo: Boolean get() = estatus == 1

    val displayCiudad: String
        get() = listOfNotNull(ciudad, estado).filter { it.isNotBlank() }.joinToString(", ")

    val displayDireccion: String
        get() = listOfNotNull(calle, numExterior).filter { it.isNotBlank() }.joinToString(" ")

    val displayColoniaCp: String
        get() = listOfNotNull(colonia, codigoPostal).filter { it.isNotBlank() }.joinToString(" · ")
}

data class ClientesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ClienteDto>? = null
)

data class ClienteDetalleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ClienteDto? = null
)

data class ClienteMutationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ClienteDto? = null,
    @SerializedName("id_cliente")
    val idCliente: Int? = null
)

data class ClienteRequest(
    @SerializedName("nombre_cliente") val nombreCliente: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String? = null,
    @SerializedName("calle") val calle: String,
    @SerializedName("num_exterior") val numExterior: String,
    @SerializedName("colonia") val colonia: String,
    @SerializedName("codigo_postal") val codigoPostal: String,
    @SerializedName("ciudad") val ciudad: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("correo") val correo: String
)

data class ClienteStatusRequest(
    @SerializedName("estatus") val estatus: Int
)

data class ClienteHistorialResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ClienteHistorialDto? = null,
    // El endpoint también puede devolver las tres secciones en la raíz.
    val ventas: List<ClienteVentaResumenDto>? = null,
    val cobros: List<ClienteCobroDto>? = null,
    val resumen: ClienteResumenDto? = null
) {
    fun historial(): ClienteHistorialDto = data ?: ClienteHistorialDto(ventas, cobros, resumen)
}

data class ClienteHistorialDto(
    val ventas: List<ClienteVentaResumenDto>? = null,
    val cobros: List<ClienteCobroDto>? = null,
    val resumen: ClienteResumenDto? = null
)

data class ClienteVentaResumenDto(
    @SerializedName("id_venta", alternate = ["id"])
    val idVenta: Int = 0,
    val codigo: String? = null,
    val fecha: String? = null,
    val total: Double = 0.0,
    @SerializedName("monto_pagado", alternate = ["pagado"])
    val montoPagado: Double = 0.0,
    val estatus: String? = null
) {
    val displayCodigo: String
        get() = codigo?.takeIf { it.isNotBlank() } ?: "V-$idVenta"
}

data class ClienteCobroDto(
    @SerializedName("id_cobro", alternate = ["id"])
    val idCobro: Int = 0,
    val fecha: String? = null,
    val monto: Double = 0.0,
    @SerializedName("forma_pago", alternate = ["forma"])
    val formaPago: String? = null,
    @SerializedName("id_venta")
    val idVenta: Int? = null,
    @SerializedName("venta_codigo", alternate = ["codigo", "referencia"])
    val ventaCodigo: String? = null
)

data class ClienteResumenDto(
    @SerializedName("total_ventas", alternate = ["ventas", "total"])
    val totalVentas: Double = 0.0,
    @SerializedName("total_cobrado", alternate = ["cobrado"])
    val totalCobrado: Double = 0.0,
    @SerializedName("saldo_pendiente", alternate = ["saldo", "adeudo"])
    val saldoPendiente: Double = 0.0
)
