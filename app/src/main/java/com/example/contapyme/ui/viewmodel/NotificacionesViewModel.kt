package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.NotificacionDto
import com.example.contapyme.data.model.ResumenNotificacionesDto
import com.example.contapyme.data.repository.NotificacionesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Categorías disponibles para filtrar las notificaciones.
 */
enum class NotificacionFiltro(
    val label: String,
    val categoria: String?
) {
    TODAS("Todas", null),
    INVENTARIO("Inventario", "inventario"),
    CXC("Cobrar", "cxc"),
    CXP("Pagar", "cxp")
}

/**
 * Estado compartido por las pantallas de notificaciones.
 */
data class NotificacionesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val consultaExitosa: Boolean = false,
    val total: Int = 0,
    val resumen: ResumenNotificacionesDto =
        ResumenNotificacionesDto(),
    val notificaciones: List<NotificacionDto> =
        emptyList(),
    val filtro: NotificacionFiltro =
        NotificacionFiltro.TODAS
) {

    // Aplica el filtro sin realizar otra petición al servidor.
    val notificacionesFiltradas: List<NotificacionDto>
        get() {
            val categoriaSeleccionada = filtro.categoria

            return if (categoriaSeleccionada == null) {
                notificaciones
            } else {
                notificaciones.filter {
                    it.categoria == categoriaSeleccionada
                }
            }
        }

    // Contadores de cuentas realmente vencidas.
    val cobrosVencidos: Int
        get() = notificaciones.count {
            it.categoria == "cxc" &&
                    it.tipo == "vencida"
        }

    val pagosVencidos: Int
        get() = notificaciones.count {
            it.categoria == "cxp" &&
                    it.tipo == "vencida"
        }
}

class NotificacionesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo =
        NotificacionesRepository(application)

    private val _uiState =
        MutableStateFlow(NotificacionesUiState())

    val uiState: StateFlow<NotificacionesUiState> =
        _uiState.asStateFlow()

    private var consultaJob: Job? = null
    private var actualizacionJob: Job? = null

    private var sesionActiva = false

    companion object {
        private const val INTERVALO_ACTUALIZACION = 60_000L
    }

    /**
     * Inicia las consultas después de la autenticación.
     */
    fun iniciarSesion() {
        if (sesionActiva) return

        limpiarSesion()

        sesionActiva = true

        cargarNotificaciones()

        iniciarActualizacionAutomatica()
    }

    /**
     * Consulta las notificaciones autorizadas.
     */
    fun cargarNotificaciones(
        mostrarCarga: Boolean = true
    ) {
        if (!sesionActiva) return

        // Evitar consultas simultáneas.
        if (consultaJob?.isActive == true) return

        consultaJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = mostrarCarga,
                    error = null
                )
            }

            repo.obtenerNotificaciones()
                .onSuccess { resultado ->

                    if (!isActive || !sesionActiva) {
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            consultaExitosa = true,
                            total = resultado.total,
                            resumen = resultado.resumen,
                            notificaciones =
                                resultado.notificaciones
                        )
                    }
                }
                .onFailure { excepcion ->

                    if (!isActive || !sesionActiva) {
                        return@onFailure
                    }

                    // No presentar información anterior
                    // como si estuviera actualizada.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            consultaExitosa = false,
                            error = excepcion.message
                                ?: "No se pudieron obtener las notificaciones",
                            total = 0,
                            resumen =
                                ResumenNotificacionesDto(),
                            notificaciones = emptyList()
                        )
                    }
                }
        }
    }

    /**
     * Actualización manual.
     */
    fun actualizar() {
        cargarNotificaciones()
    }

    /**
     * Reintenta una consulta fallida.
     */
    fun reintentar() {
        cargarNotificaciones()
    }

    /**
     * Cambia el filtro seleccionado.
     */
    fun onFiltroChange(filtro: NotificacionFiltro) {
        _uiState.update {
            it.copy(filtro = filtro)
        }
    }

    /**
     * Busca una alerta mediante su identificador real.
     */
    fun obtenerAlerta(id: String): NotificacionDto? {
        return _uiState.value.notificaciones
            .firstOrNull { it.id == id }
    }

    /**
     * Actualiza las notificaciones cada 60 segundos.
     */
    fun iniciarActualizacionAutomatica() {

        if (!sesionActiva) return

        if (actualizacionJob?.isActive == true) return

        actualizacionJob = viewModelScope.launch {

            while (isActive && sesionActiva) {

                delay(INTERVALO_ACTUALIZACION)

                if (sesionActiva) {
                    cargarNotificaciones(
                        mostrarCarga = false
                    )
                }
            }
        }
    }

    /**
     * Detiene las consultas periódicas.
     */
    fun detenerActualizacionAutomatica() {
        actualizacionJob?.cancel()
        actualizacionJob = null
    }

    /**
     * Elimina toda la información de la sesión anterior.
     */
    fun limpiarSesion() {

        sesionActiva = false

        detenerActualizacionAutomatica()

        consultaJob?.cancel()
        consultaJob = null

        _uiState.value = NotificacionesUiState()
    }
}
