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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.formatearDinero
import com.example.contapyme.ui.components.EstadoChip
import com.example.contapyme.ui.components.FilterChipRow
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.ClienteAdeudoFilter
import com.example.contapyme.ui.viewmodel.ClienteEstatusFilter
import com.example.contapyme.ui.viewmodel.ClientesViewModel

@Composable
fun ClientesScreen(
    onBack: () -> Unit,
    onAgregar: () -> Unit,
    onVerDetalle: (String) -> Unit,
    viewModel: ClientesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarClientes() }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        TopHeader(
            title = "Clientes",
            onBack = onBack,
            stats = listOf(
                "Clientes" to "${uiState.total}",
                "Activos" to "${uiState.activos}",
                "Con adeudo" to "${uiState.conAdeudo}"
            )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(BluePrimary)
                            .clickable(onClick = onAgregar)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir cliente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Buscar cliente por nombre...", color = TextLight, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
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
                        chips = listOf("Todos", "Activos", "Inactivos"),
                        selected = uiState.estatusFilter.ordinal,
                        onSelect = { index ->
                            viewModel.onEstatusFilterSelected(ClienteEstatusFilter.entries[index])
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    FilterChipRow(
                        chips = listOf("Todos los adeudos", "Con adeudo", "Sin adeudo"),
                        selected = uiState.adeudoFilter.ordinal,
                        onSelect = { index ->
                            viewModel.onAdeudoFilterSelected(ClienteAdeudoFilter.entries[index])
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Clientes", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.weight(1f))
                        Text("${uiState.clientes.size} resultados", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.cargarClientes() },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = Navy, modifier = Modifier.size(17.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            when {
                uiState.isLoading && uiState.clientes.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy)
                    }
                }

                uiState.error != null && uiState.clientes.isEmpty() -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error ?: "No fue posible consultar los clientes.", color = ErrorRed, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Revisa tu conexión y vuelve a intentarlo.", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                uiState.clientes.isEmpty() -> item {
                    Text(
                        "No hay clientes que coincidan con los filtros.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                else -> items(uiState.clientes.size) { index ->
                    val cliente = uiState.clientes[index]
                    ClienteCard(cliente = cliente, onClick = { onVerDetalle(cliente.displayId) })
                }
            }

            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun ClienteCard(cliente: ClienteDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (cliente.activo) Navy else Color(0xFF94A3B8)),
                contentAlignment = Alignment.Center
            ) {
                Text(cliente.iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cliente.nombreCompleto.ifBlank { "Cliente sin nombre" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text("${cliente.displayId} · ${cliente.displayCiudad}", fontSize = 12.sp, color = TextSecondary)
            }
            EstadoChip(if (cliente.activo) "Activo" else "Inactivo")
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(cliente.telefono ?: "—", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
            if (cliente.saldoPendiente > 0) {
                Text("Debe ${formatearDinero(cliente.saldoPendiente)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            } else {
                Text("Sin adeudo", fontSize = 12.sp, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(cliente.correo ?: "—", fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Ver detalle", color = Navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(4.dp))
    }
}
