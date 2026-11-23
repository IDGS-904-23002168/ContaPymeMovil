package com.example.contapyme.ui.screens.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.Cliente
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.AdeudoFiltro
import com.example.contapyme.ui.viewmodel.ClientesViewModel
import com.example.contapyme.ui.viewmodel.EstatusFiltro

private val FondoPantalla = Color(0xFFF4F6FB)
private val FondoSuave = Color(0xFFEEF1FA)
private val Divisor = Color(0xFFE9EDF7)
private val VerdeTexto = Color(0xFF12805C)
private val VerdeFondo = Color(0xFFE3F5EE)
private val GrisTexto = Color(0xFF6B7280)
private val GrisFondo = Color(0xFFECEEF3)
private val GradienteAzul = Brush.horizontalGradient(
    listOf(Color(0xFF2A44C9), Color(0xFF142A95), Color(0xFF0B1C66))
)

@Composable
fun ClientesScreen(
    onBack: () -> Unit,
    onAgregar: () -> Unit,
    onVerDetalle: (String) -> Unit,
    viewModel: ClientesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Recarga al volver de crear / editar / desactivar un cliente.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.reintentar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().background(FondoPantalla)) {
        TopHeader(title = "Clientes", onBack = onBack)

        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Column {
                    Spacer(Modifier.height(16.dp))

                    // Botón Añadir cliente
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(GradienteAzul)
                            .clickable(onClick = onAgregar)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir cliente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        BuscadorClientes(texto = state.busqueda, onChange = viewModel::onBusquedaChange)
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        SelectorEstatus(
                            seleccionado = state.estatus,
                            onSeleccionar = viewModel::onEstatusChange
                        )
                    }

                    // Filtros: se desplazan horizontalmente
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            text = AdeudoFiltro.CON.label,
                            selected = state.adeudo == AdeudoFiltro.CON,
                            onClick = { viewModel.onAdeudoChange(AdeudoFiltro.CON) }
                        )
                        FilterChip(
                            text = AdeudoFiltro.SIN.label,
                            selected = state.adeudo == AdeudoFiltro.SIN,
                            onClick = { viewModel.onAdeudoChange(AdeudoFiltro.SIN) }
                        )
                        FiltroDesplegable(
                            etiqueta = "Ciudad",
                            opciones = state.ciudades,
                            seleccionada = state.ciudad,
                            onSeleccionar = viewModel::onCiudadChange
                        )
                        FiltroDesplegable(
                            etiqueta = "Estado",
                            opciones = state.estados,
                            seleccionada = state.estado,
                            onSeleccionar = viewModel::onEstadoChange
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clientes", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Navy)
                        Spacer(Modifier.weight(1f))
                        if (!state.isLoading && state.error == null) {
                            val n = state.clientes.size
                            Text(
                                if (n == 1) "1 resultado" else "$n clientes",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            when {
                state.isLoading && state.clientes.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy)
                    }
                }

                state.error != null -> item {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error ?: "", color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(14.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Navy)
                                .clickable { viewModel.reintentar() }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text("Reintentar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                state.clientes.isEmpty() -> item {
                    Text(
                        "No se encontraron clientes.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }

                // Una sola tarjeta blanca con los clientes separados por una línea
                else -> itemsIndexed(state.clientes, key = { _, c -> c.id }) { index, cliente ->
                    val ultimo = index == state.clientes.lastIndex
                    val forma: Shape = when {
                        state.clientes.size == 1 -> RoundedCornerShape(20.dp)
                        index == 0 -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ultimo -> RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        else -> RectangleShape
                    }
                    ClienteFila(
                        cliente = cliente,
                        forma = forma,
                        mostrarDivisor = !ultimo,
                        onClick = { onVerDetalle(cliente.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun BuscadorClientes(texto: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (texto.isEmpty()) {
                Text("Buscar cliente por nombre...", color = TextSecondary, fontSize = 14.sp)
            }
            BasicTextField(
                value = texto,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = Navy, fontSize = 14.sp),
                cursorBrush = SolidColor(Navy),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Todos / Activos / Inactivos dentro de un contenedor suave; el activo va en azul oscuro. */
@Composable
private fun SelectorEstatus(seleccionado: EstatusFiltro, onSeleccionar: (EstatusFiltro) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FondoSuave)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EstatusFiltro.values().forEach { f ->
            val activo = f == seleccionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activo) Navy else Color.Transparent)
                    .clickable { onSeleccionar(f) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    f.label,
                    color = if (activo) Color.White else GrisTexto,
                    fontSize = 13.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }
    }
}

/** Chip: seleccionado = blanco con borde azul; normal = fondo suave. */
@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    conFlecha: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color.White else FondoSuave)
            .then(
                if (selected) Modifier.border(1.5.dp, Navy, RoundedCornerShape(20.dp)) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            color = Navy,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
        )
        if (conFlecha) {
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FiltroDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccionada: String?,
    onSeleccionar: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            text = seleccionada ?: etiqueta,
            selected = seleccionada != null,
            onClick = { abierto = true },
            conFlecha = true
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSeleccionar(null); abierto = false }
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = { onSeleccionar(opcion); abierto = false }
                )
            }
        }
    }
}

@Composable
private fun EstadoPastilla(activo: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (activo) VerdeFondo else GrisFondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            if (activo) "Activo" else "Inactivo",
            color = if (activo) VerdeTexto else GrisTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ClienteFila(
    cliente: Cliente,
    forma: Shape,
    mostrarDivisor: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(forma)
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
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
                    Text(cliente.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Text("${cliente.id} · ${cliente.ciudad}", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                EstadoPastilla(cliente.activo)
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(cliente.telefono, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                if (cliente.adeudo > 0) {
                    Text(
                        "Debe \$" + "%,.2f".format(cliente.adeudo),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                } else {
                    Text("Sin adeudo", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(cliente.correo, fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Navy, RoundedCornerShape(10.dp))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver detalle", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (mostrarDivisor) {
            Box(Modifier.padding(horizontal = 14.dp).fillMaxWidth().height(1.dp).background(Divisor))
        }
    }
}
