
package com.example.contapyme.data.repository

import android.content.Context
import com.example.contapyme.data.api.ApiClient
import com.example.contapyme.data.api.apiCall
import com.example.contapyme.data.model.GastoDto
import com.example.contapyme.data.model.GastoRequest
import com.example.contapyme.data.model.CategoriaGastoDto
import com.example.contapyme.data.model.ResumenGastosDto

class GastoRepository(context: Context) {

    private val appContext = context.applicationContext

    // Obtiene la API respetando los cambios de URL del servidor.
    private val api get() = ApiClient.getGastoApi(appContext)

    // Consultar todos los gastos.
    suspend fun listar(): Result<List<GastoDto>> =
        apiCall {
            api.getGastos()
        }.map {
            it.data
        }

    // Consultar las categorías disponibles.
    suspend fun categorias(): Result<List<CategoriaGastoDto>> =
        apiCall {
            api.getCategorias()
        }.map {
            it.data
        }

    // Consultar el resumen de gastos.
    suspend fun resumen(): Result<ResumenGastosDto> =
        apiCall {
            api.getResumen()
        }.mapCatching {
            it.data ?: error("No se pudo obtener el resumen de gastos")
        }

    // Registrar un gasto.
    suspend fun crear(request: GastoRequest): Result<GastoDto> =
        apiCall {
            api.crearGasto(request)
        }.mapCatching {
            it.data ?: error("El servidor no devolvió el gasto registrado")
        }
}
