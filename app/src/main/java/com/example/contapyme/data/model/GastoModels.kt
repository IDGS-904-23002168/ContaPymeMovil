
package com.example.contapyme.data.model

import com.google.gson.annotations.SerializedName

// Información de un gasto
data class GastoDto(
    @SerializedName("id_gasto")
    val idGasto: Int = 0,

    val fecha: String = "",
    val concepto: String = "",

    @SerializedName("id_categoria")
    val idCategoria: Int = 0,

    val categoria: String? = null,
    val monto: Double = 0.0,

    @SerializedName("forma_pago")
    val formaPago: String = "",

    val observaciones: String? = null,

    @SerializedName("id_usuario")
    val idUsuario: Int = 0,

    val estatus: String = "",

    @SerializedName("nombre_usuario")
    val nombreUsuario: String? = null,

    @SerializedName("apellido_paterno")
    val apellidoPaterno: String? = null
)

// Respuesta de listado de gastos
data class GastosListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<GastoDto> = emptyList()
)

// Respuesta de un gasto
data class GastoResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: GastoDto? = null
)

// Categoría de gasto
data class CategoriaGastoDto(
    @SerializedName("id_categoria")
    val idCategoria: Int = 0,

    val nombre: String = ""
)

// Respuesta de categorías
data class CategoriasGastosResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<CategoriaGastoDto> = emptyList()
)

// Resumen de gastos
data class ResumenGastosDto(
    @SerializedName("total_gastos")
    val totalGastos: Int = 0,

    @SerializedName("monto_total")
    val montoTotal: Double = 0.0,

    @SerializedName("gastos_cancelados")
    val gastosCancelados: Int = 0
)

// Respuesta del resumen
data class ResumenGastosResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ResumenGastosDto? = null
)

// Datos para registrar o actualizar un gasto
data class GastoRequest(
    val fecha: String,
    val concepto: String,

    @SerializedName("id_categoria")
    val idCategoria: Int,

    val monto: Double,

    @SerializedName("forma_pago")
    val formaPago: String,

    val observaciones: String? = null
)
