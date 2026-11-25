package com.example.contapyme.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.contapyme.data.model.NotificacionDto
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

data class DashboardAlertItem(
    val alertaId: String,
    val nombre: String,
    val detalle: String,
    val cantidad: String? = null,
    val monto: String? = null,
    val estado: String,
    val estadoColor: Color
)

data class DashboardAlertGroup(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val badgeCount: Int,
    val badgeColor: Color,
    val items: List<DashboardAlertItem>
)

/**
 * Convierte las notificaciones reales del backend
 * en grupos de alertas para el Dashboard.
 */
fun construirDashboardAlerts(
    notificaciones: List<NotificacionDto>
): List<DashboardAlertGroup> {

    val grupos = mutableListOf<DashboardAlertGroup>()

    fun agregarGrupo(
        alertas: List<NotificacionDto>,
        titulo: String,
        icono: ImageVector,
        color: Color
    ) {
        if (alertas.isEmpty()) return

        grupos.add(
            DashboardAlertGroup(
                icon = icono,
                iconBg = color.copy(alpha = 0.12f),
                title = titulo,
                badgeCount = alertas.size,
                badgeColor = color,
                items = alertas.take(2).map {
                    it.toDashboardAlertItem()
                }
            )
        )
    }

    // Productos sin existencias.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "inventario" &&
                    it.prioridad == "alta"
        },
        titulo = "Productos agotados",
        icono = Icons.Filled.Inventory2,
        color = ErrorRed
    )

    // Cuentas por cobrar vencidas.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "cxc" &&
                    it.tipo == "vencida"
        },
        titulo = "Cuentas por cobrar vencidas",
        icono = Icons.Filled.SouthWest,
        color = ErrorRed
    )

    // Cuentas por pagar vencidas.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "cxp" &&
                    it.tipo == "vencida"
        },
        titulo = "Pagos pendientes vencidos",
        icono = Icons.Filled.CardMembership,
        color = ErrorRed
    )

    // Productos con existencias bajas.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "inventario" &&
                    it.prioridad == "media"
        },
        titulo = "Existencias bajas",
        icono = Icons.Filled.Inventory2,
        color = WarningAmber
    )

    // Cuentas por cobrar próximas a vencer.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "cxc" &&
                    it.tipo == "proxima"
        },
        titulo = "Cobros próximos a vencer",
        icono = Icons.Filled.SouthWest,
        color = WarningAmber
    )

    // Cuentas por pagar próximas a vencer.
    agregarGrupo(
        alertas = notificaciones.filter {
            it.categoria == "cxp" &&
                    it.tipo == "proxima"
        },
        titulo = "Pagos próximos a vencer",
        icono = Icons.Filled.CardMembership,
        color = WarningAmber
    )

    return grupos
}

/**
 * Convierte una notificación en una tarjeta
 * compatible con el diseño del Dashboard.
 */
private fun NotificacionDto.toDashboardAlertItem():
        DashboardAlertItem {

    val estadoAlerta = when {
        categoria == "inventario" &&
                prioridad == "alta" -> "Agotado"

        categoria == "inventario" -> "Stock bajo"

        tipo == "vencida" -> "Vencida"

        tipo == "proxima" -> "Por vencer"

        else -> "Alerta"
    }

    val colorEstado = if (prioridad == "alta") {
        ErrorRed
    } else {
        WarningAmber
    }

    return DashboardAlertItem(
        alertaId = id,
        nombre = titulo,
        detalle = mensaje,
        cantidad = null,
        monto = saldoPendiente?.let {
            NumberFormat.getCurrencyInstance(
                Locale("es", "MX")
            ).format(it)
        },
        estado = estadoAlerta,
        estadoColor = colorEstado
    )
}
