package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.local.SessionManager
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.ProductoDto
import com.example.contapyme.data.model.VentaDto
import com.example.contapyme.data.model.VentaItemRequest
import com.example.contapyme.data.model.VentaListaDto
import com.example.contapyme.data.model.VentaRequest
import com.example.contapyme.data.repository.ClientesRepository
import com.example.contapyme.data.repository.ProductosRepository
import com.example.contapyme.data.repository.VentasRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class VentaEstatusFilter(val label: String, val apiValue: String?) {
    TODAS("Todas", null),
    PAGADAS("Pagadas", "pagada"),
    PARCIAL("Parcial", "pago_parcial"),
    PENDIENTE("Pendiente", "pendiente_pago")
}

data class VentasUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val ventas: List<VentaDto> = emptyList(),
    val total: Int = 0,
    val searchQuery: String = "",
    val estatusFilter: VentaEstatusFilter = VentaEstatusFilter.TODAS,
    val totalMes: Double = 0.0,
    val porCobrar: Double = 0.0
)

class VentasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VentasRepository(application)
    private val _uiState = MutableStateFlow(VentasUiState())
    val uiState: StateFlow<VentasUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun cargarVentas() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)
            repository.getVentas(
                query = state.searchQuery.takeIf { it.isNotBlank() },
                estatus = state.estatusFilter.apiValue
            ).onSuccess { lista: VentaListaDto ->
                val items = lista.items
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    ventas = items,
                    total = if (lista.total > 0) lista.total else items.size,
                    totalMes = items.sumOf { it.total },
                    porCobrar = items.sumOf { it.saldoPendiente }
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "No fue posible consultar las ventas."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            cargarVentas()
        }
    }

    fun onEstatusFilterSelected(filter: VentaEstatusFilter) {
        _uiState.value = _uiState.value.copy(estatusFilter = filter)
        cargarVentas()
    }
}

data class VentaDetalleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val venta: VentaDto? = null
)

class VentaDetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VentasRepository(application)
    private val _uiState = MutableStateFlow(VentaDetalleUiState())
    val uiState: StateFlow<VentaDetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = VentaDetalleUiState(isLoading = true)
            repository.getVenta(id)
                .onSuccess { venta ->
                    _uiState.value = VentaDetalleUiState(
                        isLoading = false,
                        venta = venta,
                        error = if (venta == null) "No se encontró la venta." else null
                    )
                }
                .onFailure { error ->
                    _uiState.value = VentaDetalleUiState(
                        isLoading = false,
                        error = error.message ?: "No fue posible consultar el detalle de la venta."
                    )
                }
        }
    }
}

data class ItemVentaForm(val producto: ProductoDto, val cantidad: Int = 1) {
    val subtotal: Double get() = producto.displayPrecio * cantidad
}

data class RegistrarVentaUiState(
    val isLoadingCatalogos: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val clientes: List<ClienteDto> = emptyList(),
    val productos: List<ProductoDto> = emptyList(),
    val items: List<ItemVentaForm> = emptyList(),
    val idCliente: Int? = null,
    val formaPago: String = "efectivo",
    val condicionesPago: String = "contado",
    val fechaVencimiento: String = "",
    val montoPagado: String = "",
    val observaciones: String = "",
    val folio: String? = null,
    val nombreUsuario: String? = null
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val montoPagadoValor: Double
        get() = montoPagado.replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
    val saldoPendiente: Double get() = (subtotal - montoPagadoValor).coerceAtLeast(0.0)
    val clienteSeleccionado: ClienteDto? get() = clientes.firstOrNull { it.idCliente == idCliente }
}

class RegistrarVentaViewModel(application: Application) : AndroidViewModel(application) {
    private val ventasRepository = VentasRepository(application)
    private val clientesRepository = ClientesRepository(application)
    private val productosRepository = ProductosRepository(application)
    private val sessionManager = SessionManager(application.applicationContext)

    private val _uiState = MutableStateFlow(RegistrarVentaUiState())
    val uiState: StateFlow<RegistrarVentaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val clientesResult = clientesRepository.getClientes(estatus = 1)
            val productosResult = productosRepository.getProductos()
            val folioResult = ventasRepository.getSiguienteFolio()
            val nombre = runCatching { sessionManager.userName.first() }.getOrNull()
            val error = clientesResult.exceptionOrNull() ?: productosResult.exceptionOrNull()

            _uiState.value = _uiState.value.copy(
                isLoadingCatalogos = false,
                clientes = clientesResult.getOrDefault(emptyList()),
                productos = productosResult.getOrDefault(emptyList())
                    .filter { it.estatus != 0 },
                folio = folioResult.getOrNull(),
                nombreUsuario = nombre,
                error = error?.message
            )
        }
    }

    fun onClienteSeleccionado(id: Int?) {
        _uiState.value = _uiState.value.copy(idCliente = id, error = null)
    }

    fun onFormaPagoChange(valor: String) {
        _uiState.value = _uiState.value.copy(formaPago = valor)
    }

    fun onCondicionesPagoChange(valor: String) {
        _uiState.value = _uiState.value.copy(condicionesPago = valor, error = null)
    }

    fun onFechaVencimientoChange(valor: String) {
        _uiState.value = _uiState.value.copy(fechaVencimiento = valor)
    }

    fun onMontoPagadoChange(valor: String) {
        _uiState.value = _uiState.value.copy(montoPagado = valor)
    }

    fun onObservacionesChange(valor: String) {
        _uiState.value = _uiState.value.copy(observaciones = valor)
    }

    fun agregarProducto(producto: ProductoDto) {
        val items = _uiState.value.items.toMutableList()
        val indice = items.indexOfFirst { it.producto.idProducto == producto.idProducto }
        if (indice >= 0) {
            items[indice] = items[indice].copy(cantidad = items[indice].cantidad + 1)
        } else {
            items.add(ItemVentaForm(producto))
        }
        _uiState.value = _uiState.value.copy(items = items, error = null)
    }

    fun cambiarCantidad(indice: Int, delta: Int) {
        val items = _uiState.value.items.toMutableList()
        if (indice !in items.indices) return
        val nuevaCantidad = items[indice].cantidad + delta
        if (nuevaCantidad <= 0) {
            items.removeAt(indice)
        } else {
            items[indice] = items[indice].copy(cantidad = nuevaCantidad)
        }
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun quitarProducto(indice: Int) {
        val items = _uiState.value.items.toMutableList()
        if (indice in items.indices) items.removeAt(indice)
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun guardar(onSuccess: () -> Unit) {
        val state = _uiState.value
        when {
            state.items.isEmpty() -> {
                _uiState.value = state.copy(error = "Agrega al menos un producto a la venta.")
                return
            }
            state.condicionesPago == "credito" && state.idCliente == null -> {
                _uiState.value = state.copy(error = "Las ventas a crédito requieren un cliente.")
                return
            }
            state.condicionesPago == "credito" && state.fechaVencimiento.isBlank() -> {
                _uiState.value = state.copy(error = "Indica la fecha de vencimiento de la venta a crédito.")
                return
            }
            state.montoPagadoValor > state.subtotal -> {
                _uiState.value = state.copy(error = "El monto pagado no puede ser mayor al total de la venta.")
                return
            }
        }

        val request = VentaRequest(
            idCliente = state.idCliente,
            formaPago = state.formaPago,
            condicionesPago = state.condicionesPago,
            fechaVencimiento = state.fechaVencimiento.takeIf { state.condicionesPago == "credito" },
            montoPagado = state.montoPagadoValor,
            observaciones = state.observaciones.takeIf { it.isNotBlank() },
            items = state.items.map { VentaItemRequest(it.producto.idProducto, it.cantidad) }
        )

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            ventasRepository.crearVenta(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = error.message ?: "No fue posible registrar la venta."
                    )
                }
        }
    }
}
