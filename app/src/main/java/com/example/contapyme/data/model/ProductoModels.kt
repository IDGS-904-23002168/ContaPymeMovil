package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para consultar productos e inventario desde la API de ContaPyme (/api/productos).
 * Soporta nombres alternativos (alias) para compatibilidad con distintas versiones del backend.
 */
data class ProductoDto(
    @SerializedName("id_producto", alternate = ["id", "idProducto"])
    val idProducto: Int = 0,

    @SerializedName("codigo")
    val codigo: String? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("id_categoria", alternate = ["idCategoria"])
    val idCategoria: Int? = null,

    @SerializedName("categoria", alternate = ["categoria_nombre", "nombre_categoria"])
    val categoria: String? = null,

    @SerializedName("unidad_medida", alternate = ["unidadMedida", "unidad"])
    val unidadMedida: String? = null,

    @SerializedName("precio_compra", alternate = ["precioCompra", "costo"])
    val precioCompra: Double? = null,

    @SerializedName("precio_venta", alternate = ["precioVenta", "precio"])
    val precioVenta: Double? = null,

    @SerializedName("stock", alternate = ["existencia", "existencias", "cantidad"])
    val stock: Double? = null,

    @SerializedName("stock_minimo", alternate = ["stockMinimo", "minimo"])
    val stockMinimo: Double? = null,

    @SerializedName("estatus", alternate = ["activo"])
    val estatus: Int? = null,

    @SerializedName("fecha_creacion", alternate = ["created_at"])
    val fechaCreacion: String? = null
) {
    val displayNombre: String
        get() = nombre?.takeIf { it.isNotBlank() } ?: "Producto sin nombre"

    val displayCodigo: String
        get() = codigo?.takeIf { it.isNotBlank() } ?: "SIN-COD"

    val displayCategoria: String
        get() = categoria?.takeIf { it.isNotBlank() } ?: "General"

    val displayTipo: String
        get() = tipo?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Producto"

    val displayUnidad: String
        get() = unidadMedida?.takeIf { it.isNotBlank() } ?: "pz"

    val displayPrecio: Double
        get() = precioVenta ?: 0.0

    val displayCosto: Double
        get() = precioCompra ?: 0.0

    val displayStock: Int
        get() = stock?.toInt() ?: 0

    val displayMinimo: Int
        get() = stockMinimo?.toInt() ?: 0

    val isAgotado: Boolean
        get() = displayStock <= 0

    val isStockBajo: Boolean
        get() = displayStock > 0 && displayStock <= displayMinimo

    val isDisponible: Boolean
        get() = displayStock > displayMinimo
}

data class ProductosResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ProductoDto>? = null
)

data class ProductoDetalleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ProductoDto? = null
)

data class ProductosStatsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ProductosStatsDto? = null
)

data class ProductosStatsDto(
    @SerializedName("total_productos", alternate = ["total", "productos", "totalProductos"])
    val totalProductos: Int? = null,

    @SerializedName("valor_catalogo", alternate = ["valorTotal", "total_valor", "valorCatalogo"])
    val valorCatalogo: Double? = null,

    @SerializedName("existencias_totales", alternate = ["existencias", "total_stock", "existenciasTotales"])
    val existenciasTotales: Int? = null,

    @SerializedName("stock_bajo", alternate = ["bajoStock", "productos_stock_bajo", "stockBajo"])
    val stockBajo: Int? = null,

    @SerializedName("agotados", alternate = ["productos_agotados", "totalAgotados"])
    val agotados: Int? = null
)
