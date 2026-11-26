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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.CompraDto
import com.example.contapyme.data.model.formatearDinero
import com.example.contapyme.data.model.formatearFecha
import com.example.contapyme.ui.components.EstadoChip
import com.example.contapyme.ui.components.FilterChipRow
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.CompraEstatusFilter
import com.example.contapyme.ui.viewmodel.ComprasViewModel

@Composable
fun ComprasScreen(
    onBack: () -> Unit,
    onVerDetalle: (String) -> Unit,
    viewModel: ComprasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarCompras() }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        TopHeader(
            title = "Compras",
            onBack = onBack,
            stats = listOf(
                "Compras" to "${uiState.total}",
                "Total" to formatearDinero(uiState.totalConsultado),
                "Modo" to "Consulta"
            )
        )
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Buscar por folio o proveedor...", color = TextLight, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = BorderSoft
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    FilterChipRow(
                        chips = CompraEstatusFilter.entries.map { it.label },
                        selected = uiState.estatusFilter.ordinal,
                        onSelect = { index -> viewModel.onEstatusFilterSelected(CompraEstatusFilter.entries[index]) }
                    )
                    Spacer(Modifier.height(14.dp))
                    InfoBox(background = Color(0xFFE8EEFC)) {
                        Text(
                            "Esta pantalla es de solo lectura: consulta compras y su detalle sin registrar ni modificar datos.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Compras", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.weight(1f))
                        Text("${uiState.compras.size} resultados", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.cargarCompras() },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = Navy, modifier = Modifier.size(17.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            when {
                uiState.isLoading && uiState.compras.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy)
                    }
                }

                uiState.error != null && uiState.compras.isEmpty() -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error ?: "No fue posible consultar las compras.", color = ErrorRed, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Revisa tu conexión y el permiso 'compras' de tu usuario.", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                uiState.compras.isEmpty() -> item {
                    Text(
                        "No hay compras que coincidan con la búsqueda.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                else -> items(uiState.compras.size) { index ->
                    val compra = uiState.compras[index]
                    CompraCard(compra = compra, onClick = { onVerDetalle(compra.displayCodigo) })
                }
            }

            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun CompraCard(compra: CompraDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Navy),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${compra.displayCodigo} · ${compra.displayProveedor}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(
                    "${formatearFecha(compra.fecha)} · ${compra.displayVendedor} · ${compra.itemsSeguros.size} producto${if (compra.itemsSeguros.size != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EstadoChip(compra.estadoLabel)
            Spacer(Modifier.weight(1f))
            Text(formatearDinero(compra.total), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text("Ver detalle", color = Navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
        }
    }
}
