package com.example.contapyme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.ClienteRequest
import com.example.contapyme.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campos del formulario con las mismas reglas que valida el backend (clienteValidator.js). */
enum class CampoCliente(val etiqueta: String, val max: Int, val obligatorio: Boolean) {
    NOMBRE("Nombre", 80, true),
    APELLIDO_PATERNO("Apellido paterno", 80, true),
    APELLIDO_MATERNO("Apellido materno", 80, false),
    CALLE("Calle", 120, true),
    NUM_EXTERIOR("Número exterior", 15, true),
    COLONIA("Colonia", 100, true),
    CODIGO_POSTAL("Código postal", 5, true),
    CIUDAD("Ciudad", 80, true),
    ESTADO("Estado", 60, true),
    TELEFONO("Teléfono", 10, true),
    CORREO("Correo electrónico", 120, true)
}

data class ClienteForm(
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val calle: String = "",
    val numExterior: String = "",
    val colonia: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val estado: String = "",
    val telefono: String = "",
    val correo: String = ""
) {
    fun valor(c: CampoCliente): String = when (c) {
        CampoCliente.NOMBRE -> nombre
        CampoCliente.APELLIDO_PATERNO -> apellidoPaterno
        CampoCliente.APELLIDO_MATERNO -> apellidoMaterno
        CampoCliente.CALLE -> calle
        CampoCliente.NUM_EXTERIOR -> numExterior
        CampoCliente.COLONIA -> colonia
        CampoCliente.CODIGO_POSTAL -> codigoPostal
        CampoCliente.CIUDAD -> ciudad
        CampoCliente.ESTADO -> estado
        CampoCliente.TELEFONO -> telefono
        CampoCliente.CORREO -> correo
    }

    fun con(c: CampoCliente, v: String): ClienteForm = when (c) {
        CampoCliente.NOMBRE -> copy(nombre = v)
        CampoCliente.APELLIDO_PATERNO -> copy(apellidoPaterno = v)
        CampoCliente.APELLIDO_MATERNO -> copy(apellidoMaterno = v)
        CampoCliente.CALLE -> copy(calle = v)
        CampoCliente.NUM_EXTERIOR -> copy(numExterior = v)
        CampoCliente.COLONIA -> copy(colonia = v)
        CampoCliente.CODIGO_POSTAL -> copy(codigoPostal = v)
        CampoCliente.CIUDAD -> copy(ciudad = v)
        CampoCliente.ESTADO -> copy(estado = v)
        CampoCliente.TELEFONO -> copy(telefono = v)
        CampoCliente.CORREO -> copy(correo = v)
    }
}

data class ClienteFormUiState(
    val form: ClienteForm = ClienteForm(),
    val errores: Map<CampoCliente, String> = emptyMap(),
    val isLoading: Boolean = false,   // cargando los datos del cliente (modo editar)
    val isSaving: Boolean = false,
    val error: String? = null,        // error general (del servidor o de red)
    val guardado: Boolean = false
)

private val CORREO_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

private fun limpiar(s: String) = s.trim().replace(Regex("\\s+"), " ")

private fun ClienteDto.toForm() = ClienteForm(
    nombre = nombreCliente.orEmpty(),
    apellidoPaterno = apellidoPaterno.orEmpty(),
    apellidoMaterno = apellidoMaterno.orEmpty(),
    calle = calle.orEmpty(),
    numExterior = numExterior.orEmpty(),
    colonia = colonia.orEmpty(),
    codigoPostal = codigoPostal.orEmpty(),
    ciudad = ciudad.orEmpty(),
    estado = estado.orEmpty(),
    telefono = telefono.orEmpty(),
    correo = correo.orEmpty()
)

class ClienteFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ClienteRepository(application)

    private val _uiState = MutableStateFlow(ClienteFormUiState())
    val uiState: StateFlow<ClienteFormUiState> = _uiState.asStateFlow()

    private var idCargado: String? = null

    /** Modo editar: trae los datos del cliente una sola vez. En modo agregar no hace nada. */
    fun cargar(id: String?) {
        if (id == null || id == idCargado) return
        idCargado = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.obtener(id)
                .onSuccess { c -> _uiState.update { it.copy(isLoading = false, form = c.toForm()) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onCampo(campo: CampoCliente, valor: String) {
        val v = when (campo) {
            CampoCliente.TELEFONO -> valor.filter { it.isDigit() }.take(10)
            CampoCliente.CODIGO_POSTAL -> valor.filter { it.isDigit() }.take(5)
            else -> valor.take(campo.max)
        }
        _uiState.update {
            it.copy(form = it.form.con(campo, v), errores = it.errores - campo, error = null)
        }
    }

    fun guardar(id: String?) {
        if (_uiState.value.isSaving) return

        val form = _uiState.value.form
        val errores = validar(form)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, error = "Revisa los campos marcados.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, errores = emptyMap()) }

            val request = ClienteRequest(
                nombreCliente = limpiar(form.nombre),
                apellidoPaterno = limpiar(form.apellidoPaterno),
                apellidoMaterno = limpiar(form.apellidoMaterno).ifEmpty { null },
                calle = limpiar(form.calle),
                numExterior = limpiar(form.numExterior),
                colonia = limpiar(form.colonia),
                codigoPostal = form.codigoPostal.trim(),
                ciudad = limpiar(form.ciudad),
                estado = limpiar(form.estado),
                telefono = form.telefono.trim(),
                correo = form.correo.trim().lowercase()
            )

            val resultado = if (id == null) repo.crear(request) else repo.actualizar(id, request)

            resultado
                .onSuccess { _uiState.update { it.copy(isSaving = false, guardado = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    private fun validar(f: ClienteForm): Map<CampoCliente, String> {
        val errores = mutableMapOf<CampoCliente, String>()

        for (campo in CampoCliente.values()) {
            val valor = limpiar(f.valor(campo))
            when {
                valor.isEmpty() && campo.obligatorio ->
                    errores[campo] = "El campo ${campo.etiqueta.lowercase()} es obligatorio."
                valor.length > campo.max ->
                    errores[campo] = "No puede superar ${campo.max} caracteres."
            }
        }

        if (CampoCliente.CODIGO_POSTAL !in errores && !Regex("^\\d{5}$").matches(f.codigoPostal.trim()))
            errores[CampoCliente.CODIGO_POSTAL] = "El código postal debe tener 5 dígitos."

        if (CampoCliente.TELEFONO !in errores && !Regex("^\\d{10}$").matches(f.telefono.trim()))
            errores[CampoCliente.TELEFONO] = "El teléfono debe tener 10 dígitos."

        if (CampoCliente.CORREO !in errores && !CORREO_REGEX.matches(f.correo.trim()))
            errores[CampoCliente.CORREO] = "El correo no es válido."

        return errores
    }
}
