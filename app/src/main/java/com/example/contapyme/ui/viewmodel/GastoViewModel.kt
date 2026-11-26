
package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.CategoriaGastoDto
import com.example.contapyme.data.model.GastoDto
import com.example.contapyme.data.model.GastoRequest
import com.example.contapyme.data.model.ResumenGastosDto
import com.example.contapyme.data.repository.GastoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GastoUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val gastos: List<GastoDto> = emptyList(),
    val categorias: List<CategoriaGastoDto> = emptyList(),
    val resumen: ResumenGastosDto = ResumenGastosDto()
)

class GastoViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repo = GastoRepository(application)

    private val _uiState = MutableStateFlow(GastoUiState())
    val uiState: StateFlow<GastoUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    // Cargar gastos, categorías y resumen
    fun cargarDatos() {
        cargarGastos()
        cargarCategorias()
        cargarResumen()
    }

    // Consultar gastos
    fun cargarGastos() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            repo.listar()
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            gastos = lista
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al consultar los gastos"
                        )
                    }
                }
        }
    }

    // Consultar categorías
    fun cargarCategorias() {
        viewModelScope.launch {
            repo.categorias()
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(categorias = lista)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message
                                ?: "Error al consultar las categorías"
                        )
                    }
                }
        }
    }

    // Consultar resumen
    fun cargarResumen() {
        viewModelScope.launch {
            repo.resumen()
                .onSuccess { resumen ->
                    _uiState.update {
                        it.copy(resumen = resumen)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message
                                ?: "Error al consultar el resumen"
                        )
                    }
                }
        }
    }

    // Registrar un gasto
    fun registrarGasto(request: GastoRequest) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    error = null,
                    success = null
                )
            }

            repo.crear(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            success = "Gasto registrado correctamente"
                        )
                    }

                    cargarDatos()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message
                                ?: "No se pudo registrar el gasto"
                        )
                    }
                }
        }
    }

    // Limpiar mensajes
    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                error = null,
                success = null
            )
        }
    }

    // Volver a consultar información
    fun reintentar() {
        cargarDatos()
    }
}
