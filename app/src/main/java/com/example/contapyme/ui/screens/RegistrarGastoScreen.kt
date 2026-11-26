
package com.example.contapyme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.data.model.GastoRequest
import com.example.contapyme.ui.components.BottomFooterButton
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.SpacerH12
import com.example.contapyme.ui.components.SpacerH16
import com.example.contapyme.ui.components.SpacerH4
import com.example.contapyme.ui.components.SpacerH8
import com.example.contapyme.ui.components.InlineDot
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.SuccessGreen
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.GastoViewModel
import java.time.LocalDate
import java.util.Locale

@Composable
fun RegistrarGastoScreen(
    onBack: () -> Unit,
    onGuardar: () -> Unit,
    gastoViewModel: GastoViewModel = viewModel()
) {
    val uiState by gastoViewModel.uiState.collectAsState()

    var monto by rememberSaveable { mutableStateOf("") }
    var concepto by rememberSaveable { mutableStateOf("") }
    var idCategoria by rememberSaveable { mutableStateOf(0) }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var formaPago by rememberSaveable { mutableStateOf("efectivo") }
    var observaciones by rememberSaveable { mutableStateOf("") }
    var errorFormulario by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            gastoViewModel.limpiarMensajes()
            onGuardar()
        }
    }

    val montoNumerico = monto.replace(",", ".").toDoubleOrNull() ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(GradientStart, GradientEnd)
                            )
                        )
                        .padding(top = 48.dp, bottom = 20.dp)
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                "Registrar gasto",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Nuevo registro",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    SpacerH16()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InlineDot(SuccessGreen)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Completa los datos para registrar un gasto",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // MONTO
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberedSectionHeader(1, "Monto")

                    SectionCard {
                        Text(
                            "MONTO DEL GASTO *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = TextSecondary
                        )

                        SpacerH8()

                        OutlinedTextField(
                            value = monto,
                            onValueChange = {
                                monto = it
                                errorFormulario = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Ingresa el monto")
                            },
                            leadingIcon = {
                                Text(
                                    "$",
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // DETALLE
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberedSectionHeader(2, "Detalle")

                    SectionCard {
                        Text(
                            "CONCEPTO *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = TextSecondary
                        )

                        SpacerH8()

                        OutlinedTextField(
                            value = concepto,
                            onValueChange = {
                                concepto = it
                                errorFormulario = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Describe el gasto")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        SpacerH16()

                        Text(
                            "CATEGORÍA *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = TextSecondary
                        )

                        SpacerH8()

                        if (uiState.categorias.isEmpty()) {
                            Text(
                                "No hay categorías disponibles. Comprueba la conexión con el servidor.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.categorias.chunked(2).forEach { grupo ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        grupo.forEach { categoria ->
                                            FilterChip(
                                                selected = idCategoria == categoria.idCategoria,
                                                onClick = {
                                                    idCategoria = categoria.idCategoria
                                                    errorFormulario = null
                                                },
                                                label = {
                                                    Text(categoria.nombre)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PAGO
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberedSectionHeader(3, "Pago")

                    SectionCard {
                        Text(
                            "FECHA DEL GASTO *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = TextSecondary
                        )

                        SpacerH4()

                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {
                                fecha = it
                                errorFormulario = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("AAAA-MM-DD")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            "Formato requerido: año-mes-día. Ejemplo: 2026-11-24",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        SpacerH16()

                        Text(
                            "FORMA DE PAGO *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = TextSecondary
                        )

                        SpacerH8()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val formas = listOf(
                                "efectivo" to "Efectivo",
                                "tarjeta" to "Tarjeta",
                                "transfer" to "Transferencia"
                            )

                            formas.forEach { (valor, etiqueta) ->
                                FilterChip(
                                    selected = formaPago == valor,
                                    onClick = {
                                        formaPago = valor
                                    },
                                    label = {
                                        Text(etiqueta)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        SpacerH12()

                        InfoBox {
                            Text(
                                if (formaPago == "efectivo") {
                                    "Los gastos en efectivo registran una salida en caja."
                                } else {
                                    "El gasto se registrará con la forma de pago seleccionada."
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // OBSERVACIONES
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberedSectionHeader(
                        4,
                        "Observaciones",
                        trailing = "Opcional"
                    )

                    SectionCard {
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = {
                                observaciones = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text("Escribe una nota para este gasto...")
                            },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )
                    }

                    InfoBox {
                        Text(
                            "Total del gasto: ${String.format(Locale.US, "$%,.2f", montoNumerico)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                if (errorFormulario != null || uiState.error != null) {
                    Text(
                        text = errorFormulario ?: uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(110.dp))
            }
        }

        Box(Modifier.align(Alignment.BottomCenter)) {
            BottomFooterButton(
                totalLabel = "Total gasto",
                totalValue = String.format(
                    Locale.US,
                    "$%,.2f",
                    montoNumerico
                ),
                buttonText = if (uiState.isSaving) {
                    "Guardando..."
                } else {
                    "Guardar gasto"
                },
                onClick = {
                    val cantidad = monto.replace(",", ".").toDoubleOrNull()

                    errorFormulario = when {
                        cantidad == null || cantidad <= 0 ->
                            "Ingresa un monto mayor a cero."

                        concepto.isBlank() ->
                            "Ingresa el concepto del gasto."

                        idCategoria <= 0 ->
                            "Selecciona una categoría."

                        runCatching { LocalDate.parse(fecha) }.isFailure ->
                            "Ingresa una fecha válida con formato AAAA-MM-DD."

                        else -> null
                    }

                    if (errorFormulario == null && !uiState.isSaving) {
                        val request = GastoRequest(
                            fecha = fecha,
                            concepto = concepto.trim(),
                            idCategoria = idCategoria,
                            monto = cantidad!!,
                            formaPago = formaPago,
                            observaciones = observaciones.trim()
                                .ifBlank { null }
                        )

                        gastoViewModel.registrarGasto(request)
                    }
                }
            )
        }
    }
}
