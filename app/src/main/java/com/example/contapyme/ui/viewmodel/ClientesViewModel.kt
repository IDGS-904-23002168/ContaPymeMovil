package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.Cliente
import com.example.contapyme.data.model.toUi
import com.example.contapyme.data.repository.ClienteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EstatusFiltro(val label: String, val param: String?) {
    TODOS("Todos", null),
    ACTIVOS("Activos", "1"),
    INACTIVOS("Inactivos", "0")
}

enum class AdeudoFiltro(val label: String, val param: String?) {
    TODOS("Todos", null),
    CON("Con adeudo", "con"),
    SIN("Sin adeudo", "sin")
}

data class ClientesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val clientes: List<Cliente> = emptyList(),
    val ciudades: List<String> = emptyList(),
    val estados: List<String> = emptyList(),
    val busqueda: String = "",
    val estatus: EstatusFiltro = EstatusFiltro.TODOS,
    val adeudo: AdeudoFiltro = AdeudoFiltro.TODOS,
    val ciudad: String? = null,
    val estado: String? = null
)

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ClienteRepository(application)

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    init {
        cargarOpciones()
        recargar()
    }

    fun onBusquedaChange(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        recargar(debounceMs = 350) // espera a que termine de escribir
    }

    fun onEstatusChange(valor: EstatusFiltro) {
        _uiState.update { it.copy(estatus = valor) }
        recargar()
    }

    fun onAdeudoChange(valor: AdeudoFiltro) {
        // tocar el filtro activo lo quita
        _uiState.update { it.copy(adeudo = if (it.adeudo == valor) AdeudoFiltro.TODOS else valor) }
        recargar()
    }

    fun onCiudadChange(ciudad: String?) {
        _uiState.update { it.copy(ciudad = ciudad) }
        recargar()
    }

    fun onEstadoChange(estado: String?) {
        _uiState.update { it.copy(estado = estado) }
        recargar()
    }

    fun reintentar() = recargar()

    private fun cargarOpciones() {
        viewModelScope.launch {
            repo.opciones().onSuccess { op ->
                _uiState.update { it.copy(ciudades = op.ciudades, estados = op.estados) }
            }
        }
    }

    private fun recargar(debounceMs: Long = 0) {
        job?.cancel()
        job = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            _uiState.update { it.copy(isLoading = true, error = null) }

            val s = _uiState.value
            repo.listar(
                q = s.busqueda.trim(),
                estatus = s.estatus.param,
                ciudad = s.ciudad,
                estado = s.estado,
                adeudo = s.adeudo.param
            ).onSuccess { lista ->
                _uiState.update { it.copy(isLoading = false, clientes = lista.map { d -> d.toUi() }) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
