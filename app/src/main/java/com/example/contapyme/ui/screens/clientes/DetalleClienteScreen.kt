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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.ClienteDto
import com.example.contapyme.data.model.CobroHistorialDto
import com.example.contapyme.data.model.VentaHistorialDto
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.BluePrimary
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.DetalleClienteUiState
import com.example.contapyme.ui.viewmodel.DetalleClienteViewModel

private val Verde = Color(0xFF12805C)
private val Naranja = Color(0xFFD97706)
private val Gris = Color(0xFF94A3B8)
private val FondoPantalla = Color(0xFFF4F6FB)
private val FondoIcono = Color(0xFFEEF2FB)
private val FondoTabs = Color(0xFFE8ECF6)
private val FondoTotales = Color(0xFFF8F9FC)
private val ColorDivisor = Color(0xFFEEF1F7)

private fun dinero(v: Double) = "\$" + "%,.2f".format(v)
private fun dineroCorto(v: Double) = "\$" + "%,.0f".format(v)

private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

/** "2026-03-14..." -> "14 mar 2026" */
private fun fechaCorta(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 10) return "—"
    val p = iso.take(10).split("-")
    if (p.size != 3) return iso
    val dia = p[2].toIntOrNull() ?: return iso
    val mes = p[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return iso
    return "$dia ${MESES[mes - 1]} ${p[0]}"
}

@Composable
fun DetalleClienteScreen(
    clienteId: String,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    viewModel: DetalleClienteViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var confirmar by remember { mutableStateOf(false) }

    // Carga al abrir y recarga al volver (por ejemplo después de editar).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, clienteId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.cargar(clienteId)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    val cliente = state.cliente

    Box(Modifier.fillMaxSize().background(FondoPantalla)) {
        when {
            state.isLoading -> Column(Modifier.fillMaxSize()) {
                TopHeader(title = "Detalle del cliente", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy)
                }
            }

            cliente == null -> Column(Modifier.fillMaxSize()) {
                TopHeader(title = "Detalle del cliente", onBack = onBack)
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "No se pudo cargar el cliente.",
                        color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.cargar(clienteId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Reintentar", fontWeight = FontWeight.Bold) }
                }
            }

            else -> Column(Modifier.fillMaxSize()) {
                Encabezado(cliente = cliente, state = state, onBack = onBack, onEditar = onEditar)

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SeccionEstatus(
                        activo = cliente.estatus == 1,
                        procesando = state.cambiandoEstatus,
                        onCambiar = { confirmar = true }
                    )
                    SeccionContacto(cliente)
                    SeccionDireccion(cliente)
                    SeccionHistorial(cliente, state)

                    state.error?.let { Text(it, color = ErrorRed, fontSize = 12.sp) }
                    Spacer(Modifier.height(4.dp))
                }

                BarraSaldo(saldo = cliente.saldoPendiente)
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (cliente != null) 76.dp else 0.dp)
        )
    }

    if (confirmar && cliente != null) {
        val activo = cliente.estatus == 1
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text(if (activo) "Desactivar cliente" else "Activar cliente") },
            text = {
                Text(
                    if (activo)
                        "Se conserva todo su historial, pero ya no podrá seleccionarse en nuevas ventas."
                    else
                        "El cliente podrá volver a seleccionarse en nuevas ventas."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.cambiarEstatus(clienteId, !activo)
                }) { Text("Confirmar", color = if (activo) ErrorRed else Verde) }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar", color = Navy) }
            }
        )
    }
}

/* ───────────────────────── Encabezado ───────────────────────── */

@Composable
private fun Encabezado(
    cliente: ClienteDto,
    state: DetalleClienteUiState,
    onBack: () -> Unit,
    onEditar: () -> Unit
) {
    val nombre = cliente.nombreCompleto?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(cliente.nombreCliente, cliente.apellidoPaterno, cliente.apellidoMaterno).joinToString(" ")
    val iniciales = listOfNotNull(cliente.nombreCliente, cliente.apellidoPaterno)
        .mapNotNull { it.trim().firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("").ifEmpty { "?" }
    val resumen = state.historial.resumen

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Navy, BluePrimary)))
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonCircular(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                "Detalle del cliente", color = Color.White, fontSize = 17.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
            )
            BotonCircular(Icons.Filled.Edit, "Editar cliente", onEditar)
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Cliente desde ${fechaCorta(cliente.fechaRegistro)}",
                    color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Cifra("VENTAS", state.historial.ventas.size.toString(), Modifier.weight(1f))
            DivisorVertical()
            Cifra("COBRADO", dineroCorto(resumen.totalCobrado), Modifier.weight(1f))
            DivisorVertical()
            Cifra("SALDO", dineroCorto(cliente.saldoPendiente), Modifier.weight(1f))
        }
    }
}

@Composable
private fun BotonCircular(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = descripcion, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Cifra(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 14.dp)) {
        Text(
            etiqueta, color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(valor, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DivisorVertical() {
    Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.25f)))
}

/* ───────────────────────── Secciones ───────────────────────── */

@Composable
private fun SeccionEstatus(activo: Boolean, procesando: Boolean, onCambiar: () -> Unit) {
    Seccion(1, "Estatus") {
        Tarjeta {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (activo) "Cliente activo" else "Cliente inactivo",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy
                    )
                    Text(
                        if (activo) "Puede seleccionarse en nuevas ventas"
                        else "No puede seleccionarse en nuevas ventas",
                        fontSize = 11.sp, color = TextSecondary
                    )
                }
                Switch(
                    checked = activo,
                    onCheckedChange = { onCambiar() },
                    enabled = !procesando,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Verde,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Gris,
                        uncheckedBorderColor = Gris
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.Info, null, tint = Gris,
                    modifier = Modifier.padding(top = 1.dp).size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Si lo desactivas no podrá elegirse en nuevas ventas, pero conserva su historial.",
                    fontSize = 10.sp, lineHeight = 14.sp, color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SeccionContacto(cliente: ClienteDto) {
    Seccion(2, "Contacto") {
        Tarjeta {
            FilaDato(Icons.Filled.Phone, "Teléfono", cliente.telefono?.takeIf { it.isNotBlank() } ?: "—")
            Divisor()
            FilaDato(Icons.Filled.Email, "Correo", cliente.correo?.takeIf { it.isNotBlank() } ?: "—")
        }
    }
}

@Composable
private fun SeccionDireccion(cliente: ClienteDto) {
    val calle = listOfNotNull(cliente.calle, cliente.numExterior).joinToString(" ").ifBlank { "—" }
    val colonia = listOfNotNull(cliente.colonia, cliente.codigoPostal).joinToString(" · ").ifBlank { "—" }
    val ciudad = listOfNotNull(cliente.ciudad, cliente.estado).joinToString(" · ").ifBlank { "—" }

    Seccion(3, "Dirección") {
        Tarjeta {
            FilaDato(Icons.Filled.LocationOn, "Calle y número", calle)
            Divisor()
            FilaDato(Icons.Filled.LocationOn, "Colonia · C.P.", colonia)
            Divisor()
            FilaDato(Icons.Filled.LocationOn, "Ciudad · Estado", ciudad)
        }
    }
}

@Composable
private fun SeccionHistorial(cliente: ClienteDto, state: DetalleClienteUiState) {
    var tab by remember { mutableStateOf(0) }
    val ventas = state.historial.ventas
    val cobros = state.historial.cobros
    val resumen = state.historial.resumen

    Seccion(4, "Historial de operaciones") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Pestañas
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(FondoTabs, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Pestana("Ventas", tab == 0, Modifier.weight(1f)) { tab = 0 }
                Pestana("Cobros", tab == 1, Modifier.weight(1f)) { tab = 1 }
            }

            // Solo se muestra la lista de la pestaña elegida.
            if (tab == 0) TarjetaVentas(ventas) else TarjetaCobros(cobros, ventas)

            // Totales
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(FondoTotales, RoundedCornerShape(16.dp))
                    .border(1.dp, ColorDivisor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                FilaTotal("Total vendido", dinero(resumen.totalVentas))
                Divisor()
                FilaTotal("Total cobrado", dinero(resumen.totalCobrado))
                Divisor()
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Saldo pendiente", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Navy, modifier = Modifier.weight(1f)
                    )
                    Text(
                        dinero(cliente.saldoPendiente), fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = if (cliente.saldoPendiente > 0) ErrorRed else Navy
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaVentas(ventas: List<VentaHistorialDto>) {
    Tarjeta(horizontal = 16.dp, vertical = 4.dp) {
        if (ventas.isEmpty()) {
            Vacio("Este cliente aún no tiene ventas.")
        } else {
            ventas.forEachIndexed { i, v ->
                if (i > 0) Divisor()
                val raw = v.estatus?.trim()?.lowercase().orEmpty()
                val liquidada = v.total > 0 && v.montoPagado >= v.total - 0.005
                val (etiqueta, color) = when {
                    raw == "cancelada" -> "Cancelada" to ErrorRed
                    raw == "pagada" || liquidada -> "Pagada" to Verde
                    v.montoPagado > 0 -> "Pago parcial" to Naranja
                    else -> raw.replaceFirstChar { it.uppercase() }.ifEmpty { "Pendiente" } to Gris
                }
                val subtitulo = listOfNotNull(
                    fechaCorta(v.fecha),
                    v.productos?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")

                FilaOperacion(
                    titulo = v.codigo ?: "Venta ${v.idVenta}",
                    subtitulo = subtitulo,
                    monto = dinero(v.total),
                    colorMonto = Navy,
                    etiqueta = etiqueta,
                    colorEtiqueta = color
                )
            }
        }
    }
}

@Composable
private fun TarjetaCobros(cobros: List<CobroHistorialDto>, ventas: List<VentaHistorialDto>) {
    Tarjeta(horizontal = 16.dp, vertical = 4.dp) {
        if (cobros.isEmpty()) {
            Vacio("Este cliente aún no tiene cobros.")
        } else {
            cobros.forEachIndexed { i, c ->
                if (i > 0) Divisor()
                // "Liquidado" si con este cobro la venta queda totalmente pagada; si no, "Abono".
                val etiqueta = c.idVenta?.let { idV ->
                    ventas.firstOrNull { it.idVenta == idV }?.let { venta ->
                        val acumulado = cobros
                            .filter { it.idVenta == idV && it.idCobro <= c.idCobro }
                            .sumOf { it.monto }
                        if (acumulado >= venta.total - 0.005) "Liquidado" else "Abono"
                    }
                }
                FilaOperacion(
                    titulo = "Cobro · ${c.ventaCodigo ?: c.idCobro}",
                    subtitulo = "${fechaCorta(c.fecha)} · ${c.formaPago.orEmpty().replaceFirstChar { it.uppercase() }}",
                    monto = dinero(c.monto),
                    colorMonto = Navy,
                    etiqueta = etiqueta,
                    colorEtiqueta = Verde
                )
            }
        }
    }
}

/* ───────────────────────── Barra inferior ───────────────────────── */

@Composable
private fun BarraSaldo(saldo: Double) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            "SALDO", fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp, color = TextSecondary
        )
        Text(dinero(saldo), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

/* ───────────────────────── Componentes ───────────────────────── */

/** Título numerado FUERA de la tarjeta, como en el diseño. */
@Composable
private fun Seccion(numero: Int, titulo: String, contenido: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(22.dp).background(Navy, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(numero.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        Spacer(Modifier.height(10.dp))
        contenido()
    }
}

@Composable
private fun Tarjeta(
    horizontal: Dp = 16.dp,
    vertical: Dp = 16.dp,
    contenido: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x1A0B1F4B), spotColor = Color(0x1A0B1F4B))
            .background(Color.White, forma)
            .padding(horizontal = horizontal, vertical = vertical)
    ) { contenido() }
}

@Composable
private fun FilaDato(icono: ImageVector, etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp).background(FondoIcono, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Navy, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                etiqueta.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp, color = TextSecondary
            )
            Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
    }
}

@Composable
private fun Pestana(texto: String, seleccionada: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (seleccionada) Navy else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            color = if (seleccionada) Color.White else TextSecondary
        )
    }
}

@Composable
private fun FilaOperacion(
    titulo: String,
    subtitulo: String,
    monto: String,
    colorMonto: Color,
    etiqueta: String?,
    colorEtiqueta: Color
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(subtitulo, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(monto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorMonto)
            if (etiqueta != null) {
                Text(etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorEtiqueta)
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

@Composable
private fun Divisor() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(ColorDivisor))
}

@Composable
private fun Vacio(texto: String) {
    Text(texto, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
}
