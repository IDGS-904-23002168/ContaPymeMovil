package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.ClienteHistorialDto
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.repository.ClientesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ClienteEstatusFilter { TODOS, ACTIVOS, INACTIVOS }
enum class ClienteAdeudoFilter { TODOS, CON_ADEUDO, SIN_ADEUDO }

data class ClientesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val clientes: List<ClienteDto> = emptyList(),
    val searchQuery: String = "",
    val estatusFilter: ClienteEstatusFilter = ClienteEstatusFilter.TODOS,
    val adeudoFilter: ClienteAdeudoFilter = ClienteAdeudoFilter.TODOS,
    val total: Int = 0,
    val activos: Int = 0,
    val conAdeudo: Int = 0
)

class ClientesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClientesRepository(application)
    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun cargarClientes() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)
            val estatus = when (state.estatusFilter) {
                ClienteEstatusFilter.TODOS -> null
                ClienteEstatusFilter.ACTIVOS -> 1
                ClienteEstatusFilter.INACTIVOS -> 0
            }
            val adeudo = when (state.adeudoFilter) {
                ClienteAdeudoFilter.TODOS -> null
                ClienteAdeudoFilter.CON_ADEUDO -> "con"
                ClienteAdeudoFilter.SIN_ADEUDO -> "sin"
            }
            repository.getClientes(
                query = state.searchQuery.takeIf { it.isNotBlank() },
                estatus = estatus,
                adeudo = adeudo
            ).onSuccess { lista ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    clientes = lista,
                    total = lista.size,
                    activos = lista.count { it.activo },
                    conAdeudo = lista.count { it.saldoPendiente > 0 }
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "No fue posible consultar los clientes."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            cargarClientes()
        }
    }

    fun onEstatusFilterSelected(filter: ClienteEstatusFilter) {
        _uiState.value = _uiState.value.copy(estatusFilter = filter)
        cargarClientes()
    }

    fun onAdeudoFilterSelected(filter: ClienteAdeudoFilter) {
        _uiState.value = _uiState.value.copy(adeudoFilter = filter)
        cargarClientes()
    }
}

data class ClienteDetalleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cliente: ClienteDto? = null,
    val historial: ClienteHistorialDto? = null,
    val cambiandoEstatus: Boolean = false,
    val accionError: String? = null
)

class ClienteDetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClientesRepository(application)
    private val _uiState = MutableStateFlow(ClienteDetalleUiState())
    val uiState: StateFlow<ClienteDetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = ClienteDetalleUiState(isLoading = true)
            val clienteResult = repository.getCliente(id)
            val historialResult = repository.getHistorial(id)
            val cliente = clienteResult.getOrNull()
            val error = clienteResult.exceptionOrNull() ?: historialResult.exceptionOrNull()
            _uiState.value = ClienteDetalleUiState(
                isLoading = false,
                cliente = cliente,
                historial = historialResult.getOrNull(),
                error = error?.message
            )
        }
    }

    fun cambiarEstatus(id: String, estatus: Int, onCompletado: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cambiandoEstatus = true, accionError = null)
            repository.cambiarEstatus(id, estatus)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        cambiandoEstatus = false,
                        cliente = _uiState.value.cliente?.copy(estatus = estatus)
                    )
                    onCompletado()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        cambiandoEstatus = false,
                        accionError = error.message ?: "No fue posible cambiar el estatus."
                    )
                }
        }
    }
}

data class ClienteFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val telefono: String = "",
    val correo: String = "",
    val calle: String = "",
    val numExterior: String = "",
    val codigoPostal: String = "",
    val colonia: String = "",
    val ciudad: String = "",
    val estado: String = ""
) {
    fun request(): ClienteRequest = ClienteRequest(
        nombreCliente = nombre.trim(),
        apellidoPaterno = apellidoPaterno.trim(),
        apellidoMaterno = apellidoMaterno.trim().takeIf { it.isNotBlank() },
        calle = calle.trim(),
        numExterior = numExterior.trim(),
        colonia = colonia.trim(),
        codigoPostal = codigoPostal.trim(),
        ciudad = ciudad.trim(),
        estado = estado.trim(),
        telefono = telefono.trim(),
        correo = correo.trim()
    )

    fun validacion(): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio."
        apellidoPaterno.isBlank() -> "El apellido paterno es obligatorio."
        telefono.filter { it.isDigit() }.length != 10 -> "El teléfono debe tener 10 dígitos."
        codigoPostal.filter { it.isDigit() }.length != 5 -> "El código postal debe tener 5 dígitos."
        !correo.contains("@") -> "Ingresa un correo electrónico válido."
        calle.isBlank() || numExterior.isBlank() || colonia.isBlank() || ciudad.isBlank() || estado.isBlank() ->
            "Completa todos los campos de dirección."
        else -> null
    }
}

class ClienteFormViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClientesRepository(application)
    private val _uiState = MutableStateFlow(ClienteFormUiState())
    val uiState: StateFlow<ClienteFormUiState> = _uiState.asStateFlow()

    fun cargar(id: String?) {
        if (id.isNullOrBlank()) {
            _uiState.value = ClienteFormUiState()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getCliente(id)
                .onSuccess { cliente ->
                    _uiState.value = ClienteFormUiState(
                        nombre = cliente?.nombreCliente.orEmpty(),
                        apellidoPaterno = cliente?.apellidoPaterno.orEmpty(),
                        apellidoMaterno = cliente?.apellidoMaterno.orEmpty(),
                        telefono = cliente?.telefono.orEmpty(),
                        correo = cliente?.correo.orEmpty(),
                        calle = cliente?.calle.orEmpty(),
                        numExterior = cliente?.numExterior.orEmpty(),
                        codigoPostal = cliente?.codigoPostal.orEmpty(),
                        colonia = cliente?.colonia.orEmpty(),
                        ciudad = cliente?.ciudad.orEmpty(),
                        estado = cliente?.estado.orEmpty()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "No fue posible cargar el cliente."
                    )
                }
        }
    }

    fun onFieldChange(campo: String, valor: String) {
        _uiState.value = when (campo) {
            "nombre" -> _uiState.value.copy(nombre = valor)
            "apellidoPaterno" -> _uiState.value.copy(apellidoPaterno = valor)
            "apellidoMaterno" -> _uiState.value.copy(apellidoMaterno = valor)
            "telefono" -> _uiState.value.copy(telefono = valor)
            "correo" -> _uiState.value.copy(correo = valor)
            "calle" -> _uiState.value.copy(calle = valor)
            "numExterior" -> _uiState.value.copy(numExterior = valor)
            "codigoPostal" -> _uiState.value.copy(codigoPostal = valor)
            "colonia" -> _uiState.value.copy(colonia = valor)
            "ciudad" -> _uiState.value.copy(ciudad = valor)
            "estado" -> _uiState.value.copy(estado = valor)
            else -> _uiState.value
        }.copy(error = null)
    }

    fun guardar(id: String?, onSuccess: () -> Unit) {
        val state = _uiState.value
        val validacion = state.validacion()
        if (validacion != null) {
            _uiState.value = state.copy(error = validacion)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            val result = if (id.isNullOrBlank()) {
                repository.crearCliente(state.request())
            } else {
                repository.actualizarCliente(id, state.request())
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error.message ?: "No fue posible guardar el cliente."
                )
            }
        }
    }
}
