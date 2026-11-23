package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.HistorialDto
import com.example.contapyme.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleClienteUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val cliente: ClienteDto? = null,
    val historial: HistorialDto = HistorialDto(),
    val cambiandoEstatus: Boolean = false,
    val mensaje: String? = null
)

class DetalleClienteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ClienteRepository(application)

    private val _uiState = MutableStateFlow(DetalleClienteUiState())
    val uiState: StateFlow<DetalleClienteUiState> = _uiState.asStateFlow()

    /** Se llama al abrir la pantalla y cada vez que vuelve a primer plano (por ejemplo tras editar). */
    fun cargar(id: String) {
        viewModelScope.launch { traer(id) }
    }

    private suspend fun traer(id: String) {
        // Solo muestra el spinner grande si todavía no hay nada que enseñar.
        _uiState.update { it.copy(isLoading = it.cliente == null, error = null) }

        val cliente = repo.obtener(id)
        val clienteDto = cliente.getOrNull()
        if (clienteDto == null) {
            _uiState.update { it.copy(isLoading = false, error = cliente.exceptionOrNull()?.message) }
            return
        }

        val historial = repo.historial(id)
        _uiState.update {
            it.copy(
                isLoading = false,
                cliente = clienteDto,
                historial = historial.getOrNull() ?: it.historial,
                error = historial.exceptionOrNull()?.message
            )
        }
    }

    /** RDF0402: activar/desactivar conserva todo el historial; nunca se elimina. */
    fun cambiarEstatus(id: String, activo: Boolean) {
        if (_uiState.value.cambiandoEstatus) return
        viewModelScope.launch {
            _uiState.update { it.copy(cambiandoEstatus = true) }
            repo.cambiarEstatus(id, activo)
                .onSuccess { msg ->
                    _uiState.update { it.copy(mensaje = msg) }
                    traer(id)
                }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message) } }
            _uiState.update { it.copy(cambiandoEstatus = false) }
        }
    }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }
}