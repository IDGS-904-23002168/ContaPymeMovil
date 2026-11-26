package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.ProductoDto
import com.example.contapyme.data.model.ProductosStatsDto
import com.example.contapyme.data.repository.ProductosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StockFilter(val label: String) {
    TODOS("Todos"),
    DISPONIBLES("Disponibles"),
    BAJO("Stock bajo"),
    AGOTADOS("Agotados")
}

data class InventarioUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val allProductos: List<ProductoDto> = emptyList(),
    val filteredProductos: List<ProductoDto> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: StockFilter = StockFilter.TODOS,
    val selectedProducto: ProductoDto? = null,
    val stats: ProductosStatsDto? = null,
    val totalCount: Int = 0,
    val stockBajoCount: Int = 0,
    val agotadosCount: Int = 0
)

class InventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductosRepository(application)
    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        cargarProductos()
    }

    fun cargarProductos(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )

            val result = repository.getProductos(query = _uiState.value.searchQuery)
            val statsResult = repository.getStats()

            result.onSuccess { productos ->
                val stats = statsResult.getOrNull()
                val total = stats?.totalProductos ?: productos.size
                val bajo = stats?.stockBajo ?: productos.count { it.isStockBajo }
                val agotados = stats?.agotados ?: productos.count { it.isAgotado }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    allProductos = productos,
                    stats = stats,
                    totalCount = total,
                    stockBajoCount = bajo,
                    agotadosCount = agotados
                )
                applyLocalFilters()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = err.message ?: "No fue posible consultar el inventario. Verifique su conexión y permisos."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyLocalFilters()

        // Debounce para consultar también en la API por si hay paginación del servidor
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.isNotBlank()) {
                val result = repository.getProductos(query = query)
                result.onSuccess { productos ->
                    _uiState.value = _uiState.value.copy(allProductos = productos)
                    applyLocalFilters()
                }
            } else {
                cargarProductos(isRefresh = false)
            }
        }
    }

    fun limpiarBusqueda() {
        onSearchQueryChange("")
    }

    fun onFilterSelected(filter: StockFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyLocalFilters()
    }

    fun selectProducto(producto: ProductoDto?) {
        _uiState.value = _uiState.value.copy(selectedProducto = producto)
    }

    private fun applyLocalFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val filter = state.selectedFilter

        val filtered = state.allProductos.filter { prod ->
            val matchesQuery = query.isEmpty() ||
                prod.displayNombre.lowercase().contains(query) ||
                prod.displayCodigo.lowercase().contains(query) ||
                prod.displayCategoria.lowercase().contains(query)

            val matchesFilter = when (filter) {
                StockFilter.TODOS -> true
                StockFilter.DISPONIBLES -> prod.isDisponible
                StockFilter.BAJO -> prod.isStockBajo
                StockFilter.AGOTADOS -> prod.isAgotado
            }

            matchesQuery && matchesFilter
        }

        _uiState.value = _uiState.value.copy(filteredProductos = filtered)
    }
}
