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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.ProductoDto
import com.example.contapyme.data.model.fechaHoyApi
import com.example.contapyme.data.model.formatearDinero
import com.example.contapyme.data.model.formatearFecha
import com.example.contapyme.ui.components.BottomFooterButton
import com.example.contapyme.ui.components.EstadoChip
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.SpacerH12
import com.example.contapyme.ui.components.SpacerH16
import com.example.contapyme.ui.components.SpacerH4
import com.example.contapyme.ui.components.SpacerH8
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.ItemVentaForm
import com.example.contapyme.ui.viewmodel.RegistrarVentaViewModel

@Composable
fun RegistrarVentaScreen(
    onBack: () -> Unit,
    onGuardar: () -> Unit,
    viewModel: RegistrarVentaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var clienteMenuAbierto by remember { mutableStateOf(false) }
    var productoMenuAbierto by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f)).clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Registrar venta", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    SpacerH16()
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f)).padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetaInfo("CÓDIGO DE VENTA", uiState.folio ?: "Por asignar")
                        MetaInfo("FECHA", formatearFecha(fechaHoyApi()))
                        MetaInfo("VENDEDOR", uiState.nombreUsuario ?: "—")
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(1, "Cliente (opcional)")
                    SectionCard {
                        Box {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderSoft, RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable { clienteMenuAbierto = true }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    uiState.clienteSeleccionado?.nombreCompleto ?: "Público en general",
                                    fontSize = 13.sp,
                                    color = Navy,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            DropdownMenu(
                                expanded = clienteMenuAbierto,
                                onDismissRequest = { clienteMenuAbierto = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Público en general") },
                                    onClick = {
                                        viewModel.onClienteSeleccionado(null)
                                        clienteMenuAbierto = false
                                    }
                                )
                                uiState.clientes.forEach { cliente ->
                                    DropdownMenuItem(
                                        text = { Text("${cliente.displayId} · ${cliente.nombreCompleto}") },
                                        onClick = {
                                            viewModel.onClienteSeleccionado(cliente.idCliente)
                                            clienteMenuAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                        SpacerH12()
                        InfoBox {
                            Text(
                                "Obligatorio si la venta es a crédito. En ventas de contado puedes continuar sin cliente.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(2, "Productos", trailing = "${uiState.items.size} agregados")
                    SectionCard {
                        if (uiState.isLoadingCatalogos) {
                            Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Navy, modifier = Modifier.size(24.dp))
                            }
                        }
                        uiState.items.forEachIndexed { index, item ->
                            if (index > 0) {
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                                Spacer(Modifier.height(10.dp))
                            }
                            ProductoRow(
                                item = item,
                                onCantidad = { delta -> viewModel.cambiarCantidad(index, delta) },
                                onQuitar = { viewModel.quitarProducto(index) }
                            )
                        }
                        SpacerH12()
                        Box {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .border(1.5.dp, BluePrimary, RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF4FF))
                                    .clickable { productoMenuAbierto = true }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregar producto o servicio", color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            DropdownMenu(
                                expanded = productoMenuAbierto,
                                onDismissRequest = { productoMenuAbierto = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                if (uiState.productos.isEmpty()) {
                                    DropdownMenuItem(text = { Text("No hay productos disponibles") }, onClick = { productoMenuAbierto = false })
                                }
                                uiState.productos.forEach { producto: ProductoDto ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${producto.displayNombre} · ${formatearDinero(producto.displayPrecio)} · ${producto.displayStock} pz",
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            viewModel.agregarProducto(producto)
                                            productoMenuAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(3, "Pago")
                    SectionCard {
                        Text("FORMA DE PAGO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = TextSecondary)
                        SpacerH8()
                        SegmentedSelector(
                            options = listOf("Efectivo" to Icons.Filled.Payments, "Tarjeta" to Icons.Filled.Schedule),
                            selected = if (uiState.formaPago == "efectivo") 0 else 1,
                            onSelect = { index -> viewModel.onFormaPagoChange(if (index == 0) "efectivo" else "tarjeta") }
                        )
                        SpacerH16()
                        Text("CONDICIONES DE PAGO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = TextSecondary)
                        SpacerH8()
                        SegmentedSelector(
                            options = listOf("Contado" to Icons.Filled.Schedule, "Crédito" to Icons.Filled.Schedule),
                            selected = if (uiState.condicionesPago == "contado") 0 else 1,
                            onSelect = { index -> viewModel.onCondicionesPagoChange(if (index == 0) "contado" else "credito") }
                        )
                        SpacerH16()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (uiState.condicionesPago == "credito") "VENCE *" else "VENCE (OPCIONAL)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                SpacerH4()
                                FormTextField(
                                    value = uiState.fechaVencimiento,
                                    placeholder = "AAAA-MM-DD",
                                    keyboardType = KeyboardType.Text
                                ) { viewModel.onFechaVencimientoChange(it) }
                            }
                            Column(Modifier.weight(1f)) {
                                Text("MONTO PAGADO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                SpacerH4()
                                FormTextField(
                                    value = uiState.montoPagado,
                                    placeholder = "0.00",
                                    keyboardType = KeyboardType.Decimal
                                ) { viewModel.onMontoPagadoChange(it) }
                            }
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(4, "Observaciones", trailing = "Opcional")
                    SectionCard {
                        OutlinedTextField(
                            value = uiState.observaciones,
                            onValueChange = { viewModel.onObservacionesChange(it) },
                            placeholder = { Text("Escribe una nota para esta venta...", color = TextLight) },
                            minLines = 3,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = Navy,
                                unfocusedBorderColor = BorderSoft
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberedSectionHeader(5, "Resumen")
                        Spacer(Modifier.width(8.dp))
                        EstadoChip(estatusPago(uiState.condicionesPago, uiState.montoPagadoValor, uiState.subtotal))
                    }
                    SectionCard {
                        ResumenRow("Subtotal", formatearDinero(uiState.subtotal))
                        SpacerH12()
                        ResumenRow("Monto pagado", formatearDinero(uiState.montoPagadoValor))
                        SpacerH12()
                        ResumenRow("Saldo pendiente", formatearDinero(uiState.saldoPendiente), bold = true)
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderSoft))
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
                            Spacer(Modifier.weight(1f))
                            Text(formatearDinero(uiState.subtotal), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Navy)
                        }
                    }
                    InfoBox {
                        Text(
                            "Al guardar, la API descuenta el inventario y genera una cuenta por cobrar si queda saldo pendiente.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    if (!uiState.error.isNullOrBlank()) {
                        Text(uiState.error!!, color = ErrorRed, fontSize = 12.sp)
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        Box(Modifier.align(Alignment.BottomCenter)) {
            BottomFooterButton(
                totalLabel = "Saldo",
                totalValue = formatearDinero(uiState.saldoPendiente),
                buttonText = if (uiState.isSaving) "Guardando..." else "Guardar venta",
                onClick = { viewModel.guardar(onGuardar) },
                enabled = !uiState.isSaving && !uiState.isLoadingCatalogos
            )
        }
    }
}

private fun estatusPago(condiciones: String, pagado: Double, total: Double): String = when {
    total <= 0.0 -> "Pendiente"
    pagado >= total -> "Pagada"
    pagado > 0.0 || condiciones == "credito" -> "Pago parcial"
    else -> "Pendiente"
}

@Composable
private fun FormTextField(value: String, placeholder: String, keyboardType: KeyboardType, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextLight) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedBorderColor = Navy,
            unfocusedBorderColor = BorderSoft
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MetaInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        SpacerH4()
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductoRow(item: ItemVentaForm, onCantidad: (Int) -> Unit, onQuitar: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.producto.displayNombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
                SpacerH4()
                Text(
                    "${item.producto.displayCodigo} · Disp. ${item.producto.displayStock} pz",
                    fontSize = 11.sp,
                    color = if (item.producto.isStockBajo) Color(0xFFB45309) else TextSecondary
                )
            }
            Text(formatearDinero(item.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        SpacerH8()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.RemoveCircle, contentDescription = "Quitar una unidad", tint = BluePrimary,
                modifier = Modifier.size(26.dp).clickable { onCantidad(-1) }
            )
            Box(
                modifier = Modifier.padding(horizontal = 10.dp).clip(RoundedCornerShape(8.dp))
                    .background(BluePrimary).padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("${item.cantidad}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Icon(
                Icons.Filled.AddCircle, contentDescription = "Agregar una unidad", tint = BluePrimary,
                modifier = Modifier.size(26.dp).clickable { onCantidad(1) }
            )
            Spacer(Modifier.width(10.dp))
            Text("× ${formatearDinero(item.producto.displayPrecio)}", fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.Delete, contentDescription = "Eliminar producto", tint = TextSecondary,
                modifier = Modifier.size(18.dp).clickable(onClick = onQuitar)
            )
        }
    }
}

@Composable
fun SegmentedSelector(
    options: List<Pair<String, ImageVector>>,
    selected: Int,
    onSelect: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9)).padding(4.dp)
    ) {
        options.forEachIndexed { index, (label, icon) ->
            val isSelected = index == selected
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Navy else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = if (isSelected) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun ResumenRow(label: String, value: String, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (bold) Navy else TextSecondary,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = Navy)
    }
}
