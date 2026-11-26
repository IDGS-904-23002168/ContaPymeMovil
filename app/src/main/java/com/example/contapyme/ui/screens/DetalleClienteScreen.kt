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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.ClienteCobroDto
import com.example.contapyme.data.model.ClienteHistorialDto
import com.example.contapyme.data.model.ClienteVentaResumenDto
import com.example.contapyme.data.model.formatearDinero
import com.example.contapyme.data.model.formatearFecha
import com.example.contapyme.ui.components.BottomFooterButton
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.SpacerH12
import com.example.contapyme.ui.components.SpacerH16
import com.example.contapyme.ui.components.SpacerH8
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BlueSoft
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.SuccessGreen
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.theme.WarningAmber
import com.example.contapyme.ui.viewmodel.ClienteDetalleViewModel

@Composable
fun DetalleClienteScreen(
    id: String,
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    onVerVenta: (String) -> Unit,
    viewModel: ClienteDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) { viewModel.cargar(id) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Navy)
            }

            uiState.error != null || uiState.cliente == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    uiState.error ?: "No se encontró el cliente.",
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }

            else -> {
                val cliente = uiState.cliente!!
                val historial = uiState.historial
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                                .padding(top = 48.dp, bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable(onClick = onBack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("Detalle del cliente", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            }
                            SpacerH16()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cliente.iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(cliente.nombreCompleto, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${cliente.displayId} · Desde ${formatearFecha(cliente.fechaRegistro)}",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.12f)).padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Kpi("VENTAS", "${historial?.ventas?.size ?: 0}")
                                Kpi("COBRADO", formatearDinero(historial?.resumen?.totalCobrado))
                                Kpi("SALDO", formatearDinero(cliente.saldoPendiente))
                            }
                        }
                    }

                    item {
                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberedSectionHeader(1, "Estatus")
                            SectionCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = !uiState.cambiandoEstatus) {
                                        viewModel.cambiarEstatus(cliente.displayId, if (cliente.activo) 0 else 1) {}
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            if (cliente.activo) "Cliente activo" else "Cliente inactivo",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy
                                        )
                                        Text("Toca para cambiar el estatus", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier.width(52.dp).height(30.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(if (cliente.activo) SuccessGreen else Color(0xFF94A3B8)),
                                        contentAlignment = if (cliente.activo) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Box(Modifier.padding(horizontal = 3.dp).size(24.dp).clip(CircleShape).background(Color.White))
                                    }
                                }
                                SpacerH12()
                                InfoBox(background = Color(0xFFF1F5F9)) {
                                    Text(
                                        "El cambio de estatus se registra en la API (PATCH /clientes/:id/status).",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (!uiState.accionError.isNullOrBlank()) {
                                    SpacerH8()
                                    Text(uiState.accionError!!, color = ErrorRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberedSectionHeader(2, "Contacto")
                            SectionCard {
                                ContactRow(Icons.Filled.Phone, "TELÉFONO", cliente.telefono ?: "—")
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                                Spacer(Modifier.height(10.dp))
                                ContactRow(Icons.Filled.Email, "CORREO", cliente.correo ?: "—")
                            }
                        }
                    }

                    item {
                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberedSectionHeader(3, "Dirección")
                            SectionCard {
                                ContactRow(Icons.Filled.Place, "CALLE Y NÚMERO", cliente.displayDireccion.ifBlank { "—" })
                                Spacer(Modifier.height(10.dp))
                                ContactRow(Icons.Filled.Place, "COLONIA · C.P.", cliente.displayColoniaCp.ifBlank { "—" })
                                Spacer(Modifier.height(10.dp))
                                ContactRow(Icons.Filled.Place, "CIUDAD · ESTADO", cliente.displayCiudad.ifBlank { "—" })
                            }
                        }
                    }

                    item {
                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberedSectionHeader(4, "Historial de operaciones")
                            SectionCard {
                                Text("VENTAS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                SpacerH8()
                                val ventas = historial?.ventas.orEmpty()
                                if (ventas.isEmpty()) {
                                    Text("Sin ventas registradas.", fontSize = 12.sp, color = TextSecondary)
                                } else {
                                    ventas.forEachIndexed { index, venta ->
                                        if (index > 0) Divider()
                                        VentaHistorialRow(venta) { onVerVenta(venta.displayCodigo) }
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Text("COBROS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                SpacerH8()
                                val cobros = historial?.cobros.orEmpty()
                                if (cobros.isEmpty()) {
                                    Text("Sin cobros registrados.", fontSize = 12.sp, color = TextSecondary)
                                } else {
                                    cobros.forEachIndexed { index, cobro ->
                                        if (index > 0) Divider()
                                        CobroHistorialRow(cobro)
                                    }
                                }
                            }
                            SpacerH12()
                            SectionCard {
                                TotalRow("Total vendido", formatearDinero(historial?.resumen?.totalVentas))
                                SpacerH12()
                                TotalRow("Total cobrado", formatearDinero(historial?.resumen?.totalCobrado))
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Saldo pendiente", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        formatearDinero(historial?.resumen?.saldoPendiente ?: cliente.saldoPendiente),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                }
                            }
                            if (historial == null && uiState.error == null) {
                                InfoBox(background = Color(0xFFF1F5F9)) {
                                    Text(
                                        "El historial financiero se consulta desde /api/clientes/:id/historial.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }

                Box(Modifier.align(Alignment.BottomCenter)) {
                    BottomFooterButton(
                        totalLabel = "Saldo",
                        totalValue = formatearDinero(cliente.saldoPendiente),
                        buttonText = "Editar cliente",
                        onClick = { onEditar(cliente.displayId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun VentaHistorialRow(venta: ClienteVentaResumenDto, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(venta.displayCodigo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("${formatearFecha(venta.fecha)} · ${venta.estatus.orEmpty()}", fontSize = 11.sp, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatearDinero(venta.total), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(
                if ((venta.total - venta.montoPagado) > 0) "Pendiente" else "Pagada",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if ((venta.total - venta.montoPagado) > 0) WarningAmber else SuccessGreen
            )
        }
    }
}

@Composable
private fun CobroHistorialRow(cobro: ClienteCobroDto) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Cobro · ${cobro.ventaCodigo ?: "V-${cobro.idVenta ?: 0}"}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("${formatearFecha(cobro.fecha)} · ${cobro.formaPago.orEmpty()}", fontSize = 11.sp, color = TextSecondary)
        }
        Text(formatearDinero(cobro.monto), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

@Composable
private fun Kpi(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(BlueSoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Navy)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}
