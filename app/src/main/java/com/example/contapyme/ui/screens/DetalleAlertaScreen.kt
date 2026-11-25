package com.example.contapyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contapyme.data.model.NotificacionDto
import com.example.contapyme.ui.components.BottomFooterButton
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.StatusChip
import com.example.contapyme.ui.components.PrimaryButton
import com.example.contapyme.ui.theme.BlueSoft
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.theme.WarningAmber
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.viewmodel.NotificacionesUiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetalleAlertaScreen(
    onBack: () -> Unit,
    onVerVenta: () -> Unit = {},
    alertaId: String = "",
    uiState: NotificacionesUiState = NotificacionesUiState(),
    onReintentar: () -> Unit = {}
) {

    // Buscar la alerta real dentro del estado compartido.
    val alerta = uiState.notificaciones.firstOrNull {
        it.id == alertaId
    }

    val colorAlerta = alerta?.let {
        obtenerColorAlerta(it)
    } ?: Navy

    val saldo = alerta?.saldoPendiente

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Encabezado original de la pantalla.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(top = 48.dp, bottom = 18.dp)
                    .padding(horizontal = 20.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color.White.copy(alpha = 0.15f)
                            )
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "Detalle de alerta",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mostrar metadatos únicamente cuando
                // la alerta esté disponible.
                if (alerta != null && uiState.consultaExitosa) {

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color.White.copy(alpha = 0.12f)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 12.dp
                            ),
                        horizontalArrangement =
                            Arrangement.SpaceEvenly
                    ) {

                        Meta(
                            label = "ID ALERTA",
                            value = alerta.id,
                            modifier = Modifier.weight(1f)
                        )

                        Meta(
                            label = "PRIORIDAD",
                            value = obtenerPrioridad(alerta),
                            modifier = Modifier.weight(1f)
                        )

                        Meta(
                            label = "REFERENCIA",
                            value = alerta.referencia
                                ?.takeIf { it.isNotBlank() }
                                ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Spacer(Modifier.height(16.dp))

                when {

                    // Error al consultar las notificaciones.
                    uiState.error != null -> {

                        EstadoDetalleAlerta(
                            titulo = "No se pudo consultar la alerta",
                            mensaje = uiState.error
                                ?: "Ocurrió un error inesperado",
                            esError = true,
                            onReintentar = onReintentar,
                            onBack = onBack
                        )
                    }

                    // Carga inicial.
                    !uiState.consultaExitosa -> {

                        SectionCard {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(30.dp),
                                    color = Navy
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Consultando alerta...",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // La alerta ya no existe o no está
                    // disponible para el usuario.
                    alerta == null -> {

                        EstadoDetalleAlerta(
                            titulo = "Alerta no disponible",
                            mensaje = "La alerta pudo haberse resuelto " +
                                    "o ya no está disponible para tu sesión.",
                            esError = false,
                            onReintentar = onReintentar,
                            onBack = onBack
                        )
                    }

                    // Mostrar información real de la alerta.
                    else -> {

                        // SECCIÓN 1: Mensaje de la alerta.
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    NumberedSectionHeader(
                                        number = 1,
                                        title = "Mensaje"
                                    )
                                }

                                StatusChip(
                                    text = obtenerEstadoAlerta(alerta),
                                    container = colorAlerta.copy(
                                        alpha = 0.12f
                                    ),
                                    content = colorAlerta
                                )
                            }

                            SectionCard {

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(colorAlerta),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Icon(
                                            imageVector =
                                                obtenerIconoAlerta(alerta),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = alerta.titulo,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy
                                        )

                                        Spacer(Modifier.height(3.dp))

                                        Text(
                                            text = "Tipo: " +
                                                    obtenerCategoriaAlerta(alerta),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = alerta.mensaje,
                                    fontSize = 13.sp,
                                    color = Navy,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        // SECCIÓN 2: Información relacionada.
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            NumberedSectionHeader(
                                2,
                                if (alerta.categoria == "inventario") {
                                    "Producto relacionado"
                                } else if (
                                    alerta.categoria == "cxc" ||
                                    alerta.categoria == "cxp"
                                ) {
                                    "Cuenta relacionada"
                                } else {
                                    "Información relacionada"
                                }
                            )

                            SectionCard {

                                // Categoría real de la notificación.
                                CuentaRow(
                                    icon = obtenerIconoAlerta(alerta),
                                    label = "CATEGORÍA",
                                    value = obtenerCategoriaAlerta(alerta),
                                    valueColor = Navy
                                )

                                SeparadorDetalle()

                                // Identificador del registro relacionado.
                                CuentaRow(
                                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                    label = when (alerta.categoria) {
                                        "inventario" -> "ID PRODUCTO"
                                        "cxc" -> "ID CUENTA POR COBRAR"
                                        "cxp" -> "ID CUENTA POR PAGAR"
                                        else -> "ID RELACIONADO"
                                    },
                                    value = if (alerta.idReferencia > 0) {
                                        alerta.idReferencia.toString()
                                    } else {
                                        "No disponible"
                                    },
                                    valueColor = Navy
                                )

                                // Cliente o proveedor, cuando el
                                // mensaje del backend lo proporciona.
                                obtenerEntidadRelacionada(alerta)
                                    ?.let { entidad ->

                                        SeparadorDetalle()

                                        CuentaRow(
                                            icon = Icons.Filled.Person,
                                            label = if (
                                                alerta.categoria == "cxc"
                                            ) {
                                                "CLIENTE"
                                            } else {
                                                "PROVEEDOR"
                                            },
                                            value = entidad,
                                            valueColor = Navy
                                        )
                                    }

                                // Referencia de producto, venta o compra.
                                alerta.referencia
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { referencia ->

                                        SeparadorDetalle()

                                        CuentaRow(
                                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                            label = when (
                                                alerta.categoria
                                            ) {
                                                "inventario" ->
                                                    "CÓDIGO DEL PRODUCTO"

                                                "cxc" ->
                                                    "REFERENCIA DE VENTA"

                                                "cxp" ->
                                                    "REFERENCIA DE COMPRA"

                                                else ->
                                                    "REFERENCIA"
                                            },
                                            value = referencia,
                                            valueColor = Navy
                                        )
                                    }

                                // Fecha de vencimiento real.
                                alerta.fechaVencimiento
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { fecha ->

                                        SeparadorDetalle()

                                        CuentaRow(
                                            icon =
                                                Icons.Filled.CalendarToday,
                                            label = "VENCIMIENTO",
                                            value = formatearFechaDetalle(
                                                fecha
                                            ),
                                            valueColor = colorAlerta
                                        )
                                    }

                                // Saldo pendiente real.
                                alerta.saldoPendiente?.let { monto ->

                                    SeparadorDetalle()

                                    CuentaRow(
                                        icon = Icons.Filled.Schedule,
                                        label = "SALDO PENDIENTE",
                                        value = formatearMontoDetalle(monto),
                                        valueColor = colorAlerta
                                    )
                                }

                                SeparadorDetalle()

                                // Prioridad real.
                                CuentaRow(
                                    icon = Icons.Filled.Info,
                                    label = "PRIORIDAD",
                                    value = obtenerPrioridad(alerta),
                                    valueColor = colorAlerta
                                )
                            }
                        }

                        // Información sobre la generación de alertas.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BlueSoft)
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {

                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = obtenerMensajeInformativo(alerta),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Permite actualizar el detalle sin salir.
                        TextButton(
                            onClick = onReintentar,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = if (uiState.isLoading) {
                                    "Actualizando..."
                                } else {
                                    "Actualizar información"
                                },
                                color = BluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Espacio para el botón inferior.
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }

        // Pie de pantalla con información real.
        if (alerta != null && uiState.consultaExitosa) {

            Box(
                Modifier.align(Alignment.BottomCenter)
            ) {

                if (saldo != null) {

                    BottomFooterButton(
                        totalLabel = "Saldo pendiente",
                        totalValue = formatearMontoDetalle(saldo),
                        buttonText = "Regresar",
                        onClick = onBack
                    )

                } else {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {

                        PrimaryButton(
                            text = "Regresar",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Metadatos del encabezado.
 */
@Composable
private fun Meta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(3.dp))

        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Fila de información relacionada.
 */
@Composable
private fun CuentaRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BlueSoft),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = TextSecondary
            )

            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

/**
 * Separador visual de los datos.
 */
@Composable
private fun SeparadorDetalle() {

    Spacer(Modifier.height(12.dp))

    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSoft)
    )

    Spacer(Modifier.height(12.dp))
}

/**
 * Pantalla de error o alerta no disponible.
 */
@Composable
private fun EstadoDetalleAlerta(
    titulo: String,
    mensaje: String,
    esError: Boolean,
    onReintentar: () -> Unit,
    onBack: () -> Unit
) {

    SectionCard {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                modifier = Modifier.size(38.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (esError) ErrorRed else Navy,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "Reintentar",
                onClick = onReintentar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "Regresar",
                    color = BluePrimary
                )
            }
        }
    }
}

/**
 * Obtiene el nombre visible de la categoría.
 */
private fun obtenerCategoriaAlerta(
    alerta: NotificacionDto
): String = when (alerta.categoria) {

    "inventario" -> "Inventario"

    "cxc" -> "Cuentas por cobrar"

    "cxp" -> "Cuentas por pagar"

    else -> "General"
}

/**
 * Obtiene la prioridad visible.
 */
private fun obtenerPrioridad(
    alerta: NotificacionDto
): String = when (alerta.prioridad) {

    "alta" -> "Alta"

    "media" -> "Media"

    "baja" -> "Baja"

    else -> "No especificada"
}

/**
 * Obtiene el estado real de la alerta.
 */
private fun obtenerEstadoAlerta(
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
 * Obtiene el color de acuerdo con la prioridad.
 */
private fun obtenerColorAlerta(
    alerta: NotificacionDto
): Color = when (alerta.prioridad) {

    "alta" -> ErrorRed

    "media" -> WarningAmber

    else -> TextSecondary
}

/**
 * Obtiene el icono correspondiente a la alerta.
 */
private fun obtenerIconoAlerta(
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
 * Extrae el cliente o proveedor del mensaje real.
 *
 * El backend actualmente devuelve estos datos dentro
 * del mensaje y no como propiedades independientes.
 */
private fun obtenerEntidadRelacionada(
    alerta: NotificacionDto
): String? {

    val prefijo = when (alerta.categoria) {

        "cxc" -> "Cliente:"

        "cxp" -> "Proveedor:"

        else -> return null
    }

    if (!alerta.mensaje.startsWith(
            prefijo,
            ignoreCase = true
        )
    ) {
        return null
    }

    return alerta.mensaje
        .substring(prefijo.length)
        .trim()
        .removeSuffix(".")
        .trim()
        .takeIf { it.isNotBlank() }
}

/**
 * Explica por qué se genera cada tipo de alerta.
 */
private fun obtenerMensajeInformativo(
    alerta: NotificacionDto
): String = when (alerta.categoria) {

    "inventario" ->
        "Las alertas de inventario se generan cuando " +
                "las existencias de un producto son iguales " +
                "o inferiores a su stock mínimo."

    "cxc" ->
        "Las alertas de cuentas por cobrar se generan " +
                "cuando existe un saldo pendiente y la cuenta " +
                "está vencida o vence en los próximos 7 días."

    "cxp" ->
        "Las alertas de cuentas por pagar se generan " +
                "cuando existe un saldo pendiente y la cuenta " +
                "está vencida o vence en los próximos 7 días."

    else ->
        "Esta alerta se genera a partir de la información " +
                "disponible en el sistema."
}

/**
 * Formatea los saldos en pesos mexicanos.
 */
private fun formatearMontoDetalle(
    monto: Double
): String {

    return NumberFormat
        .getCurrencyInstance(Locale("es", "MX"))
        .format(monto)
}

/**
 * Convierte la fecha ISO a dd/MM/yyyy.
 */
private fun formatearFechaDetalle(
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
