package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

/** Contratos de /api/compras. La app solo consume consultas (GET). */
data class CompraItemDto(
    @SerializedName("id_detalle_compra", alternate = ["id"])
    val idDetalleCompra: Int = 0,
    @SerializedName("id_producto", alternate = ["idProducto"])
    val idProducto: Int = 0,
    @SerializedName("producto", alternate = ["nombre_producto", "nombre", "descripcion"])
    val producto: String? = null,
    @SerializedName("codigo", alternate = ["codigo_producto"])
    val codigo: String? = null,
    val cantidad: Double = 0.0,
    @SerializedName("costo_unitario", alternate = ["precio_unitario", "costo", "precio"])
    val costoUnitario: Double = 0.0,
    val subtotal: Double = 0.0
) {
    val displayNombre: String get() = producto?.takeIf { it.isNotBlank() } ?: "Producto $idProducto"
    val displayCodigo: String get() = codigo?.takeIf { it.isNotBlank() } ?: "PRD-$idProducto"
    val importe: Double get() = if (subtotal != 0.0) subtotal else cantidad * costoUnitario
}

data class CompraDto(
    @SerializedName("id_compra", alternate = ["id"])
    val idCompra: Int = 0,
    val codigo: String? = null,
    val fecha: String? = null,
    @SerializedName("id_proveedor")
    val idProveedor: Int? = null,
    @SerializedName("proveedor", alternate = ["nombre_proveedor", "nombre_empresa", "proveedor_nombre"])
    val proveedor: String? = null,
    @SerializedName("id_usuario")
    val idUsuario: Int? = null,
    @SerializedName("vendedor", alternate = ["nombre_usuario", "usuario", "nombre_vendedor"])
    val vendedor: String? = null,
    @SerializedName("condiciones_pago", alternate = ["condicionesPago"])
    val condicionesPago: String? = null,
    @SerializedName("fecha_vencimiento", alternate = ["vencimiento"])
    val fechaVencimiento: String? = null,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val estatus: String? = null,
    val observaciones: String? = null,
    @SerializedName("items", alternate = ["detalles", "productos"])
    val items: List<CompraItemDto>? = null
) {
    val displayCodigo: String
        get() = codigo?.takeIf { it.isNotBlank() } ?: "COM-$idCompra"

    val displayProveedor: String
        get() = proveedor?.takeIf { it.isNotBlank() } ?: "Proveedor $idProveedor"

    val displayVendedor: String
        get() = vendedor?.takeIf { it.isNotBlank() } ?: "—"

    val estadoLabel: String
        get() = when (estatus?.lowercase()) {
            "pagada", "pagado" -> "Pagada"
            "parcial", "pago_parcial" -> "Pago parcial"
            "cancelada", "cancelado" -> "Cancelada"
            else -> "Pendiente"
        }

    val itemsSeguros: List<CompraItemDto> get() = items.orEmpty()
}

data class CompraPaginacionDto(
    val total: Int = 0,
    val pagina: Int = 1,
    val limite: Int = 50,
    @SerializedName("total_paginas", alternate = ["totalPaginas"])
    val totalPaginas: Int = 1
)

data class ComprasDataDto(
    val items: List<CompraDto> = emptyList(),
    val paginacion: CompraPaginacionDto? = null
)

data class ComprasResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ComprasDataDto? = null
)

data class CompraDetalleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CompraDto? = null
)
