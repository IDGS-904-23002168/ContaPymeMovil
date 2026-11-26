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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import com.example.contapyme.data.model.VentaDto
import com.example.contapyme.data.model.formatearDinero
import com.example.contapyme.data.model.formatearFecha
import com.example.contapyme.ui.components.EstadoChip
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.SpacerH12
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BlueSoft
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.VentaDetalleViewModel

@Composable
fun DetalleVentaScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: VentaDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) { viewModel.cargar(id) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .padding(top = 48.dp, bottom = 18.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Detalle de venta", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Meta("CÓDIGO DE VENTA", uiState.venta?.displayCodigo ?: "—")
                Meta("FECHA", formatearFecha(uiState.venta?.fecha))
                Meta("VENDEDOR", uiState.venta?.displayVendedor ?: "—")
            }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Navy)
            }

            uiState.error != null || uiState.venta == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    uiState.error ?: "No se encontró la venta.",
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }

            else -> DetalleVentaContent(venta = uiState.venta!!)
        }
    }
}

@Composable
private fun DetalleVentaContent(venta: VentaDto) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedSectionHeader(1, "Cliente")
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BlueSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = BluePrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("CLIENTE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, color = TextSecondary)
                            Text(venta.displayCliente, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedSectionHeader(2, "Productos", trailing = "${venta.itemsSeguros.size} productos")
                SectionCard {
                    if (venta.itemsSeguros.isEmpty()) {
                        Text("Sin productos registrados.", color = TextSecondary, fontSize = 12.sp)
                    }
                    venta.itemsSeguros.forEachIndexed { index, item ->
                        if (index > 0) {
                            Spacer(Modifier.height(10.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                            Spacer(Modifier.height(10.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.displayNombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
                                Text(
                                    "${item.displayCodigo} · ${item.cantidad.toInt()} × ${formatearDinero(item.precioUnitario)}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(formatearDinero(item.importe), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedSectionHeader(3, "Pago")
                SectionCard {
                    InfoLine(Icons.Filled.CreditCard, "FORMA DE PAGO", venta.formaPago?.replaceFirstChar { it.uppercase() } ?: "—")
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                    Spacer(Modifier.height(10.dp))
                    InfoLine(Icons.Filled.Schedule, "CONDICIONES", venta.condicionesPago?.replaceFirstChar { it.uppercase() } ?: "—")
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                    Spacer(Modifier.height(10.dp))
                    InfoLine(Icons.Filled.CalendarToday, "VENCE", formatearFecha(venta.fechaVencimiento))
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberedSectionHeader(4, "Resumen")
                    Spacer(Modifier.width(8.dp))
                    EstadoChip(venta.estadoLabel)
                }
                SectionCard {
                    ResumenRow("Subtotal", formatearDinero(venta.subtotal))
                    SpacerH12()
                    ResumenRow("Monto pagado", formatearDinero(venta.montoPagado))
                    SpacerH12()
                    ResumenRow("Saldo pendiente", formatearDinero(venta.saldoPendiente), bold = true)
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.weight(1f))
                        Text(formatearDinero(venta.total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                    }
                    if (!venta.observaciones.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Observaciones: ${venta.observaciones}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                if (venta.saldoPendiente > 0) {
                    InfoBox {
                        Text(
                            "Esta venta tiene un saldo pendiente de ${formatearDinero(venta.saldoPendiente)}.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun Meta(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(BlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
    }
}

@Composable
private fun ResumenRow(label: String, value: String, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = if (bold) Navy else TextSecondary, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = Navy)
    }
}
