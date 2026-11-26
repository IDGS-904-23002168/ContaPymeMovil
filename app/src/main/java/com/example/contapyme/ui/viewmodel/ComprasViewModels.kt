package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.CompraDto
import com.example.contapyme.data.repository.ComprasRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CompraEstatusFilter(val label: String, val apiValue: String?) {
    TODAS("Todas", null),
    PAGADAS("Pagadas", "pagada"),
    PARCIAL("Parcial", "parcial"),
    PENDIENTE("Pendiente", "pendiente_pago"),
    CANCELADA("Cancelada", "cancelada")
}

data class ComprasUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val compras: List<CompraDto> = emptyList(),
    val total: Int = 0,
    val searchQuery: String = "",
    val estatusFilter: CompraEstatusFilter = CompraEstatusFilter.TODAS,
    val totalConsultado: Double = 0.0
)

/** Solo lectura: este ViewModel no expone ninguna operación de escritura. */
class ComprasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ComprasRepository(application)
    private val _uiState = MutableStateFlow(ComprasUiState())
    val uiState: StateFlow<ComprasUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun cargarCompras() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)
            repository.getCompras(
                query = state.searchQuery.takeIf { it.isNotBlank() },
                estatus = state.estatusFilter.apiValue
            ).onSuccess { data ->
                val items = data.items
                val totalServidor = data.paginacion?.total ?: 0
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    compras = items,
                    total = if (totalServidor > 0) totalServidor else items.size,
                    totalConsultado = items.sumOf { it.total }
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "No fue posible consultar las compras."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            cargarCompras()
        }
    }

    fun onEstatusFilterSelected(filter: CompraEstatusFilter) {
        _uiState.value = _uiState.value.copy(estatusFilter = filter)
        cargarCompras()
    }
}

data class CompraDetalleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val compra: CompraDto? = null
)

class CompraDetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ComprasRepository(application)
    private val _uiState = MutableStateFlow(CompraDetalleUiState())
    val uiState: StateFlow<CompraDetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = CompraDetalleUiState(isLoading = true)
            repository.getCompra(id)
                .onSuccess { compra ->
                    _uiState.value = CompraDetalleUiState(
                        isLoading = false,
                        compra = compra,
                        error = if (compra == null) "No se encontró la compra." else null
                    )
                }
                .onFailure { error ->
                    _uiState.value = CompraDetalleUiState(
                        isLoading = false,
                        error = error.message ?: "No fue posible consultar el detalle de la compra."
                    )
                }
        }
    }
}
