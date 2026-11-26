
package com.example.contapyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.GastoDto
import com.example.contapyme.ui.viewmodel.GastoViewModel
import java.text.NumberFormat
import java.util.Locale

private val AzulGastos = Color(0xFF1D3FA5)
private val FondoGastos = Color(0xFFF4F6FB)
private val TextoGastos = Color(0xFF172554)
private val GrisGastos = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(
    onBack: () -> Unit,
    onRegistrar: () -> Unit,
    gastoViewModel: GastoViewModel = viewModel()
) {
    val uiState by gastoViewModel.uiState.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var filtroEstatus by remember { mutableStateOf("Todos") }

    val gastosFiltrados = uiState.gastos.filter { gasto ->
        val coincideBusqueda =
            gasto.concepto.contains(busqueda, ignoreCase = true)

        val coincideEstatus = when (filtroEstatus) {
            "Activos" -> gasto.estatus.equals("activo", ignoreCase = true)
            "Cancelados" -> gasto.estatus.equals("cancelado", ignoreCase = true)
            else -> true
        }

        coincideBusqueda && coincideEstatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGastos)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulGastos)
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Gastos",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Registro y control de gastos",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = { gastoViewModel.cargarDatos() }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = onRegistrar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulGastos
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar gasto")
                }
            }

            item {
                Text(
                    "Resumen de gastos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoGastos
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ResumenGastoCard(
                        titulo = "Total de gastos",
                        valor = uiState.resumen.totalGastos.toString()
                    )
                    ResumenGastoCard(
                        titulo = "Monto total",
                        valor = formatoMoneda(uiState.resumen.montoTotal)
                    )
                    ResumenGastoCard(
                        titulo = "Gastos cancelados",
                        valor = uiState.resumen.gastosCancelados.toString()
                    )
                }
            }

            item {
                Text(
                    "Historial de gastos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoGastos
                )
            }

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por concepto...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulGastos,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Todos", "Activos", "Cancelados").forEach { filtro ->
                        FilterChip(
                            selected = filtroEstatus == filtro,
                            onClick = { filtroEstatus = filtro },
                            label = { Text(filtro) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AzulGastos,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AzulGastos)
                    }
                }
            }

            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp
                    )
                    TextButton(onClick = { gastoViewModel.reintentar() }) {
                        Text("Reintentar")
                    }
                }
            }

            if (!uiState.isLoading && gastosFiltrados.isEmpty()) {
                item {
                    Text(
                        if (uiState.gastos.isEmpty())
                            "Todavía no hay gastos registrados."
                        else
                            "No se encontraron gastos con esos filtros.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        color = GrisGastos
                    )
                }
            }

            items(gastosFiltrados, key = { it.idGasto }) { gasto ->
                GastoHistorialCard(gasto)
            }

            item {
                Text(
                    "${gastosFiltrados.size} gastos encontrados",
                    color = GrisGastos,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumenGastoCard(
    titulo: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, color = GrisGastos, fontSize = 13.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                valor,
                color = TextoGastos,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GastoHistorialCard(gasto: GastoDto) {
    val activo = gasto.estatus.equals("activo", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        gasto.concepto,
                        color = TextoGastos,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "ID: ${gasto.idGasto} · ${gasto.fecha}",
                        color = GrisGastos,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (activo) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        gasto.estatus.ifBlank { "Sin estatus" },
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        color = if (activo) Color(0xFF15803D) else Color(0xFFB91C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(10.dp))

            Text(
                "Categoría: ${gasto.categoria ?: "Sin categoría"}",
                color = GrisGastos,
                fontSize = 13.sp
            )
            Text(
                "Forma de pago: ${gasto.formaPago}",
                color = GrisGastos,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                formatoMoneda(gasto.monto),
                color = TextoGastos,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            if (!gasto.observaciones.isNullOrBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    "Observaciones: ${gasto.observaciones}",
                    color = GrisGastos,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatoMoneda(monto: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(monto)
}
