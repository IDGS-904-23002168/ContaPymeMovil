package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

/** Contratos de /api/ventas: consulta, detalle y registro. */
data class VentaItemDto(
    @SerializedName("id_detalle_venta", alternate = ["id"])
    val idDetalleVenta: Int = 0,
    @SerializedName("id_producto", alternate = ["idProducto"])
    val idProducto: Int = 0,
    @SerializedName("producto", alternate = ["nombre_producto", "nombre", "descripcion"])
    val producto: String? = null,
    @SerializedName("codigo", alternate = ["codigo_producto"])
    val codigo: String? = null,
    val cantidad: Double = 0.0,
    @SerializedName("precio_unitario", alternate = ["precio", "precio_venta"])
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
) {
    val displayNombre: String get() = producto?.takeIf { it.isNotBlank() } ?: "Producto $idProducto"
    val displayCodigo: String get() = codigo?.takeIf { it.isNotBlank() } ?: "PRD-$idProducto"
    val importe: Double get() = if (subtotal != 0.0) subtotal else cantidad * precioUnitario
}

data class VentaDto(
    @SerializedName("id_venta", alternate = ["id"])
    val idVenta: Int = 0,
    val codigo: String? = null,
    val fecha: String? = null,
    @SerializedName("id_cliente")
    val idCliente: Int? = null,
    @SerializedName("cliente", alternate = ["nombre_cliente", "cliente_nombre"])
    val cliente: String? = null,
    @SerializedName("id_usuario")
    val idUsuario: Int? = null,
    @SerializedName("vendedor", alternate = ["nombre_usuario", "usuario", "nombre_vendedor"])
    val vendedor: String? = null,
    @SerializedName("forma_pago", alternate = ["formaPago"])
    val formaPago: String? = null,
    @SerializedName("condiciones_pago", alternate = ["condicionesPago"])
    val condicionesPago: String? = null,
    @SerializedName("fecha_vencimiento", alternate = ["vencimiento"])
    val fechaVencimiento: String? = null,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    @SerializedName("monto_pagado", alternate = ["pagado"])
    val montoPagado: Double = 0.0,
    val estatus: String? = null,
    val observaciones: String? = null,
    @SerializedName("items", alternate = ["detalles", "productos"])
    val items: List<VentaItemDto>? = null
) {
    val displayCodigo: String
        get() = codigo?.takeIf { it.isNotBlank() } ?: "V-$idVenta"

    val displayCliente: String
        get() = cliente?.takeIf { it.isNotBlank() } ?: "Público en general"

    val displayVendedor: String
        get() = vendedor?.takeIf { it.isNotBlank() } ?: "—"

    val saldoPendiente: Double
        get() = (total - montoPagado).coerceAtLeast(0.0)

    val estadoLabel: String
        get() = when (estatus?.lowercase()) {
            "pagada", "pagado" -> "Pagada"
            "pago_parcial", "parcial" -> "Pago parcial"
            "cancelada", "cancelado" -> "Cancelada"
            else -> "Pendiente"
        }

    val itemsSeguros: List<VentaItemDto> get() = items.orEmpty()
}

data class VentaListaDto(
    val total: Int = 0,
    val items: List<VentaDto> = emptyList()
)

data class VentasResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: VentaListaDto? = null
)

data class VentaDetalleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: VentaDto? = null
)

/** /api/ventas/siguiente-folio puede devolver un string o un objeto con el folio. */
data class VentaFolioResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: com.google.gson.JsonElement? = null,
    val codigo: String? = null,
    val folio: String? = null
)

data class VentaCreadaResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: VentaDto? = null,
    @SerializedName("id_venta")
    val idVenta: Int? = null,
    val codigo: String? = null
)

data class VentaItemRequest(
    @SerializedName("id_producto") val idProducto: Int,
    @SerializedName("cantidad") val cantidad: Int
)

data class VentaRequest(
    @SerializedName("id_cliente") val idCliente: Int? = null,
    @SerializedName("forma_pago") val formaPago: String = "efectivo",
    @SerializedName("condiciones_pago") val condicionesPago: String = "contado",
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerializedName("monto_pagado") val montoPagado: Double = 0.0,
    @SerializedName("observaciones") val observaciones: String? = null,
    @SerializedName("items") val items: List<VentaItemRequest>
)

data class VentaCreada(
    val idVenta: Int,
    val codigo: String?,
    val mensaje: String?
)
