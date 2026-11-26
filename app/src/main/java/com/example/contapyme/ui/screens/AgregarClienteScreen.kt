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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.ui.components.InfoBox
import com.example.contapyme.ui.components.NumberedSectionHeader
import com.example.contapyme.ui.components.PrimaryButton
import com.example.contapyme.ui.components.SecondaryButton
import com.example.contapyme.ui.components.SectionCard
import com.example.contapyme.ui.components.SpacerH16
import com.example.contapyme.ui.components.SpacerH8
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.GradientEnd
import com.example.contapyme.ui.theme.GradientStart
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.ClienteFormViewModel

@Composable
fun AgregarClienteScreen(
    onBack: () -> Unit,
    onGuardar: () -> Unit,
    clienteId: String? = null,
    viewModel: ClienteFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val esEdicion = !clienteId.isNullOrBlank()

    LaunchedEffect(clienteId) { viewModel.cargar(clienteId) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                        .padding(top = 48.dp, bottom = 24.dp)
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
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (esEdicion) "Editar cliente" else "Agregar cliente",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(1, "Datos personales")
                    SectionCard {
                        FormTextField("Nombre(s)", uiState.nombre, "Ej. María Fernanda") { viewModel.onFieldChange("nombre", it) }
                        SpacerH16()
                        FormTextField("Apellido paterno", uiState.apellidoPaterno, "Ej. López") {
                            viewModel.onFieldChange("apellidoPaterno", it)
                        }
                        SpacerH16()
                        FormTextField("Apellido materno", uiState.apellidoMaterno, "Ej. Soto (opcional)") {
                            viewModel.onFieldChange("apellidoMaterno", it)
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(2, "Contacto")
                    SectionCard {
                        FormTextField(
                            "Teléfono", uiState.telefono, "10 dígitos",
                            keyboardType = KeyboardType.Phone
                        ) { viewModel.onFieldChange("telefono", it) }
                        SpacerH16()
                        FormTextField(
                            "Correo electrónico", uiState.correo, "correo@ejemplo.com",
                            keyboardType = KeyboardType.Email
                        ) { viewModel.onFieldChange("correo", it) }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberedSectionHeader(3, "Dirección")
                    SectionCard {
                        FormTextField("Calle", uiState.calle, "Nombre de la calle") { viewModel.onFieldChange("calle", it) }
                        SpacerH16()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                FormTextField("Núm. exterior", uiState.numExterior, "123") {
                                    viewModel.onFieldChange("numExterior", it)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                FormTextField("C.P.", uiState.codigoPostal, "37000", KeyboardType.Number) {
                                    viewModel.onFieldChange("codigoPostal", it)
                                }
                            }
                        }
                        SpacerH16()
                        FormTextField("Colonia", uiState.colonia, "Nombre de la colonia") {
                            viewModel.onFieldChange("colonia", it)
                        }
                        SpacerH16()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                FormTextField("Ciudad", uiState.ciudad, "León") {
                                    viewModel.onFieldChange("ciudad", it)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                FormTextField("Estado", uiState.estado, "Guanajuato") {
                                    viewModel.onFieldChange("estado", it)
                                }
                            }
                        }
                    }
                    InfoBox {
                        Text(
                            if (esEdicion)
                                "El identificador del cliente no se modifica. El estatus se cambia desde el detalle."
                            else
                                "El cliente se registrará con estatus Activo y quedará asociado a tu empresa.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(110.dp)) }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (!uiState.error.isNullOrBlank()) {
                Text(uiState.error!!, color = ErrorRed, fontSize = 12.sp)
                SpacerH8()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(text = "Cancelar", onClick = onBack, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = "Guardar cliente",
                    onClick = { viewModel.guardar(clienteId, onGuardar) },
                    modifier = Modifier.weight(1.4f),
                    enabled = !uiState.isSaving && !uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
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
}
