package com.example.contapyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contapyme.data.model.NotificacionDto
import com.example.contapyme.ui.components.StatusChip
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.theme.WarningAmber
import com.example.contapyme.ui.viewmodel.NotificacionFiltro
import com.example.contapyme.ui.viewmodel.NotificacionesUiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NotificacionesScreen(
    onBack: () -> Unit,
    onVerDetalle: (String) -> Unit,
    uiState: NotificacionesUiState = NotificacionesUiState(),
    onFiltroChange: (NotificacionFiltro) -> Unit = {},
    onActualizar: () -> Unit = {},
    onReintentar: () -> Unit = {}
) {

    // Obtener las notificaciones de acuerdo con el filtro.
    val alertasFiltradas = uiState.notificacionesFiltradas

    // Agrupar las alertas por prioridad real.
    val alertasAlta = alertasFiltradas.filter {
        it.prioridad == "alta"
    }

    val alertasMedia = alertasFiltradas.filter {
        it.prioridad == "media"
    }

    val otrasAlertas = alertasFiltradas.filter {
        it.prioridad != "alta" &&
                it.prioridad != "media"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
    ) {

        // Encabezado con contadores reales.
        TopHeader(
            title = "Notificaciones",
            onBack = onBack,
            stats = listOf(
                "Inventario" to if (uiState.consultaExitosa) {
                    uiState.resumen.inventario.toString()
                } else {
                    "—"
                },

                "Por cobrar" to if (uiState.consultaExitosa) {
                    uiState.resumen.cxc.toString()
                } else {
                    "—"
                },

                "Por pagar" to if (uiState.consultaExitosa) {
                    uiState.resumen.cxp.toString()
                } else {
                    "—"
                }
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            // Filtros funcionales.
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    NotificacionesFilterRow(
                        seleccionado = uiState.filtro,
                        onFiltroChange = onFiltroChange
                    )
                }
            }

            // Resumen y actualización manual.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = if (uiState.consultaExitosa) {
                            "${alertasFiltradas.size} de " +
                                    "${uiState.total} alertas"
                        } else {
                            "Notificaciones"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = onActualizar,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            text = "Actualizar",
                            color = Navy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            when {

                // Error al consultar el backend.
                uiState.error != null -> {
                    item {
                        EstadoNotificaciones(
                            titulo = "No se pudieron cargar las alertas",
                            mensaje = uiState.error
                                ?: "Ocurrió un error inesperado",
                            esError = true,
                            onReintentar = onReintentar
                        )
                    }
                }

                // Carga inicial.
                !uiState.consultaExitosa -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = Navy
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Consultando notificaciones...",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // No existen alertas para el filtro seleccionado.
                alertasFiltradas.isEmpty() -> {
                    item {
                        EstadoNotificaciones(
                            titulo = if (
                                uiState.filtro ==
                                NotificacionFiltro.TODAS
                            ) {
                                "No hay notificaciones"
                            } else {
                                "No hay alertas en esta categoría"
                            },
                            mensaje = if (
                                uiState.filtro ==
                                NotificacionFiltro.TODAS
                            ) {
                                "No se encontraron alertas disponibles " +
                                        "para tu sesión."
                            } else {
                                "Selecciona otra categoría para " +
                                        "consultar las demás alertas."
                            },
                            esError = false
                        )
                    }
                }

                // Mostrar notificaciones reales.
                else -> {

                    // Indicador de actualización manual.
                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement =
                                    Arrangement.Center,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Navy,
                                    strokeWidth = 2.dp
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    "Actualizando...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Alertas de prioridad alta.
                    if (alertasAlta.isNotEmpty()) {

                        item {
                            EncabezadoGrupoAlertas(
                                titulo = "Prioridad alta",
                                cantidad = alertasAlta.size,
                                color = ErrorRed
                            )
                        }

                        items(
                            count = alertasAlta.size,
                            key = { index ->
                                alertasAlta[index].id
                            }
                        ) { index ->

                            val alerta = alertasAlta[index]

                            AlertaCard(
                                alerta = alerta,
                                onClick = {
                                    onVerDetalle(alerta.id)
                                }
                            )
                        }
                    }

                    // Alertas de prioridad media.
                    if (alertasMedia.isNotEmpty()) {

                        item {
                            EncabezadoGrupoAlertas(
                                titulo = "Prioridad media",
                                cantidad = alertasMedia.size,
                                color = WarningAmber
                            )
                        }

                        items(
                            count = alertasMedia.size,
                            key = { index ->
                                alertasMedia[index].id
                            }
                        ) { index ->

                            val alerta = alertasMedia[index]

                            AlertaCard(
                                alerta = alerta,
                                onClick = {
                                    onVerDetalle(alerta.id)
                                }
                            )
                        }
                    }

                    // Permitir otras prioridades si el backend
                    // incorpora nuevos tipos de notificaciones.
                    if (otrasAlertas.isNotEmpty()) {

                        item {
                            EncabezadoGrupoAlertas(
                                titulo = "Otras alertas",
                                cantidad = otrasAlertas.size,
                                color = TextSecondary
                            )
                        }

                        items(
                            count = otrasAlertas.size,
                            key = { index ->
                                otrasAlertas[index].id
                            }
                        ) { index ->

                            val alerta = otrasAlertas[index]

                            AlertaCard(
                                alerta = alerta,
                                onClick = {
                                    onVerDetalle(alerta.id)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

/**
 * Filtros exclusivos del módulo de notificaciones.
 *
 * No se modifica FilterChipRow de Common.kt para
 * evitar afectar otros módulos.
 */
@Composable
private fun NotificacionesFilterRow(
    seleccionado: NotificacionFiltro,
    onFiltroChange: (NotificacionFiltro) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        NotificacionFiltro.values().forEach { filtro ->

            val activo = seleccionado == filtro

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (activo) Navy else Color.White
                    )
                    .border(
                        width = 1.dp,
                        color = if (activo) {
                            Navy
                        } else {
                            Color(0xFFE2E8F0)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        onFiltroChange(filtro)
                    }
                    .padding(
                        horizontal = 18.dp,
                        vertical = 9.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filtro.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activo) {
                        Color.White
                    } else {
                        Navy
                    }
                )
            }
        }
    }
}

/**
 * Encabezado de cada grupo de notificaciones.
 */
@Composable
private fun EncabezadoGrupoAlertas(
    titulo: String,
    cantidad: Int,
    color: Color
) {

    Column(
        Modifier.padding(horizontal = 16.dp)
    ) {

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = if (cantidad == 1) {
                    "1 alerta"
                } else {
                    "$cantidad alertas"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Tarjeta de notificación con información real.
 */
@Composable
private fun AlertaCard(
    alerta: NotificacionDto,
    onClick: () -> Unit
) {

    val colorAlerta = iconColorFor(alerta)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {

        Row(
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorAlerta),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(alerta),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    text = alerta.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = alerta.mensaje,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Referencia real, cuando está disponible.
                alerta.referencia
                    ?.takeIf { it.isNotBlank() }
                    ?.let { referencia ->

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Referencia: $referencia",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                // Saldo pendiente de cuentas por cobrar o pagar.
                alerta.saldoPendiente?.let { saldo ->

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Saldo pendiente: ${formatearMonto(saldo)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy
                    )
                }

                // Fecha real de vencimiento.
                alerta.fechaVencimiento
                    ?.takeIf { it.isNotBlank() }
                    ?.let { fecha ->

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Vencimiento: ${formatearFecha(fecha)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            StatusChip(
                text = estadoFor(alerta),
                container = colorAlerta.copy(alpha = 0.12f),
                content = colorAlerta
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = when (alerta.categoria) {
                    "inventario" -> "Inventario"
                    "cxc" -> "Por cobrar"
                    "cxp" -> "Por pagar"
                    else -> "General"
                },
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null,
                tint = Navy,
                modifier = Modifier.size(15.dp)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = "Ver detalle",
                color = Navy,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Navy,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Estado vacío o error de consulta.
 */
@Composable
private fun EstadoNotificaciones(
    titulo: String,
    mensaje: String,
    esError: Boolean,
    onReintentar: (() -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = if (esError) {
                Icons.Filled.Close
            } else {
                Icons.Filled.Notifications
            },
            contentDescription = null,
            tint = if (esError) {
                ErrorRed
            } else {
                TextSecondary
            },
            modifier = Modifier.size(36.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (esError) ErrorRed else Navy,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = mensaje,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (onReintentar != null) {

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onReintentar
            ) {
                Text(
                    text = "Reintentar",
                    color = Navy,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Icono correspondiente a la categoría y prioridad.
 */
private fun iconFor(
    alerta: NotificacionDto
): ImageVector = when {

    alerta.categoria == "inventario" &&
            alerta.prioridad == "alta" ->
        Icons.Filled.Close

    alerta.categoria == "inventario" ->
        Icons.Filled.Inventory2

    alerta.categoria == "cxc" ->
        Icons.Filled.SouthWest

    alerta.categoria == "cxp" ->
        Icons.Filled.CardMembership

    else ->
        Icons.Filled.Notifications
}

/**
 * Color correspondiente a la prioridad.
 */
private fun iconColorFor(
    alerta: NotificacionDto
): Color = when (alerta.prioridad) {

    "alta" -> ErrorRed

    "media" -> WarningAmber

    else -> TextSecondary
}

/**
 * Estado visible de la notificación.
 */
private fun estadoFor(
    alerta: NotificacionDto
): String = when {

    alerta.categoria == "inventario" &&
            alerta.prioridad == "alta" -> "Agotado"

    alerta.categoria == "inventario" -> "Stock bajo"

    alerta.tipo == "vencida" -> "Vencida"

    alerta.tipo == "proxima" -> "Por vencer"

    else -> "Alerta"
}

/**
 * Formatea los saldos en pesos mexicanos.
 */
private fun formatearMonto(
    monto: Double
): String {

    return NumberFormat
        .getCurrencyInstance(Locale("es", "MX"))
        .format(monto)
}

/**
 * Convierte una fecha ISO del backend a dd/MM/yyyy.
 * No modifica la fecha mediante conversiones de zona horaria.
 */
private fun formatearFecha(
    fecha: String
): String {

    val partes = fecha
        .take(10)
        .split("-")

    return if (
        partes.size == 3 &&
        partes[0].length == 4 &&
        partes[1].length == 2 &&
        partes[2].length == 2 &&
        partes.all { parte ->
            parte.all { it.isDigit() }
        }
    ) {
        "${partes[2]}/${partes[1]}/${partes[0]}"
    } else {
        fecha
    }
}
