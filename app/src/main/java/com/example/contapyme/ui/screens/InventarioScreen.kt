package com.example.contapyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.ProductoDto
import com.example.contapyme.ui.components.FilterChipRow
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.SuccessGreen
import com.example.contapyme.ui.theme.TextDark
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.theme.WarningAmber
import com.example.contapyme.ui.viewmodel.InventarioViewModel
import com.example.contapyme.ui.viewmodel.StockFilter

@Composable
fun InventarioScreen(
    onBack: () -> Unit,
    viewModel: InventarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedProductoId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
    ) {
        // Encabezado principal con métricas en tiempo real de la API
        TopHeader(
            title = "Inventario",
            onBack = onBack,
            stats = listOf(
                "Productos" to "${uiState.totalCount}",
                "Stock bajo" to "${uiState.stockBajoCount}",
                "Agotados" to "${uiState.agotadosCount}"
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))

                    // Barra interactiva de búsqueda (nombre, código o categoría)
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = {
                            Text(
                                text = "Buscar por nombre, código o categoría...",
                                color = TextLight,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Buscar",
                                tint = if (uiState.searchQuery.isNotEmpty()) Navy else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.limpiarBusqueda() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = BorderSoft,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    // Filtros por estado de stock
                    val chipLabels = listOf(
                        "Todos (${uiState.allProductos.size})",
                        "Disponibles",
                        "Stock bajo (${uiState.stockBajoCount})",
                        "Agotados (${uiState.agotadosCount})"
                    )

                    FilterChipRow(
                        chips = chipLabels,
                        selected = uiState.selectedFilter.ordinal,
                        onSelect = { index ->
                            viewModel.onFilterSelected(StockFilter.entries[index])
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Barra de conteo de resultados y botón de refresco
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Catálogo de Productos",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${uiState.filteredProductos.size} resultados",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.cargarProductos(isRefresh = true) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Actualizar",
                                tint = Navy,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }
            }

            // Estados de la consulta a la API
            when {
                // Estado 1: Cargando datos
                uiState.isLoading -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Navy,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Consultando catálogo en ContaPyme...",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Estado 2: Error al conectar con la API
                uiState.error != null && uiState.allProductos.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
                                .padding(20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Error al consultar inventario",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = uiState.error ?: "No fue posible conectar con el servidor.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F1D1D),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.cargarProductos(isRefresh = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reintentar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Estado 3: Sin coincidencias tras búsqueda o filtro
                uiState.filteredProductos.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SearchOff,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Sin productos encontrados",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) {
                                    "No hay productos que coincidan con \"${uiState.searchQuery}\"."
                                } else {
                                    "No hay productos registrados con este filtro de stock."
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            if (uiState.searchQuery.isNotBlank() || uiState.selectedFilter != StockFilter.TODOS) {
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.limpiarBusqueda()
                                        viewModel.onFilterSelected(StockFilter.TODOS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Restablecer filtros", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Estado 4: Lista de productos consultados con éxito
                else -> {
                    items(
                        items = uiState.filteredProductos,
                        key = { it.idProducto.takeIf { id -> id > 0 } ?: it.displayCodigo }
                    ) { producto ->
                        val isExpanded = expandedProductoId == producto.idProducto
                        ProductoCard(
                            producto = producto,
                            expanded = isExpanded,
                            onToggleExpand = {
                                expandedProductoId = if (isExpanded) null else producto.idProducto
                            }
                        )
                    }
                }
            }

            // Nota informativa de consulta (sin CRUD)
            item {
                Column(Modifier.padding(16.dp)) {
                    InfoBox {
                        Text(
                            text = "Consulta de inventario en tiempo real conectada a la API de ContaPyme. Toca cualquier producto para desplegar su ficha técnica y niveles de stock.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(Modifier.height(90.dp))
                }
            }
        }
    }
}

/**
 * Ficha de producto en modo solo lectura / consulta.
 * Permite desplegar detalles extendidos como precio de compra, stock mínimo y tipo de producto.
 */
@Composable
private fun ProductoCard(
    producto: ProductoDto,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onToggleExpand)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono de inventario con badge indicador
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            producto.isAgotado -> Color(0xFFFEE2E2)
                            producto.isStockBajo -> Color(0xFFFEF3C7)
                            else -> Color(0xFFDBEAFE)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = null,
                    tint = when {
                        producto.isAgotado -> ErrorRed
                        producto.isStockBajo -> WarningAmber
                        else -> BluePrimary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = producto.displayNombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${producto.displayCodigo} · ${producto.displayCategoria}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$" + "%,.2f".format(producto.displayPrecio),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Spacer(Modifier.height(2.dp))
                // Badge de stock con unidad
                Text(
                    text = "${producto.displayStock} ${producto.displayUnidad}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        producto.isAgotado -> ErrorRed
                        producto.isStockBajo -> WarningAmber
                        else -> SuccessGreen
                    }
                )
            }
        }

        // Ficha técnica expandida con datos adicionales de consulta
        if (expanded) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(label = "EXISTENCIA", value = "${producto.displayStock} ${producto.displayUnidad}", valueColor = Navy)
                MiniStat(label = "MÍNIMO", value = "${producto.displayMinimo} ${producto.displayUnidad}", valueColor = Navy)
                MiniStat(
                    label = "ESTADO",
                    value = when {
                        producto.isAgotado -> "Agotado"
                        producto.isStockBajo -> "Stock bajo"
                        else -> "Disponible"
                    },
                    valueColor = when {
                        producto.isAgotado -> ErrorRed
                        producto.isStockBajo -> WarningAmber
                        else -> SuccessGreen
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Datos económicos y de clasificación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tipo: ${producto.displayTipo}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (producto.displayCosto > 0) {
                    Text(
                        text = "Costo compra: $" + "%,.2f".format(producto.displayCosto),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!producto.descripcion.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = producto.descripcion,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
