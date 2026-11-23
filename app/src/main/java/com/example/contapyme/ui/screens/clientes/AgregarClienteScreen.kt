package com.example.contapyme.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contapyme.ui.components.TopHeader
import com.example.contapyme.ui.theme.BorderSoft
import com.example.contapyme.ui.theme.ErrorRed
import com.example.contapyme.ui.theme.Navy
import com.example.contapyme.ui.theme.TextLight
import com.example.contapyme.ui.theme.TextSecondary
import com.example.contapyme.ui.viewmodel.CampoCliente
import com.example.contapyme.ui.viewmodel.ClienteFormViewModel

private val FondoPantalla = Color(0xFFF4F6FB)
private val FondoCampo = Color(0xFFF7F8FC)
private val FondoInfo = Color(0xFFE9EEF9)

/**
 * Sirve para AGREGAR (clienteId = null) y para EDITAR (clienteId = "C-0004").
 * Mantiene el nombre del parámetro onGuardar para que tu MainActivity actual siga compilando.
 */
@Composable
fun AgregarClienteScreen(
    onBack: () -> Unit,
    onGuardar: () -> Unit,
    clienteId: String? = null,
    viewModel: ClienteFormViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val editando = clienteId != null

    LaunchedEffect(clienteId) { viewModel.cargar(clienteId) }

    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            Toast.makeText(
                context,
                if (editando) "Cliente actualizado correctamente." else "Cliente registrado exitosamente.",
                Toast.LENGTH_SHORT
            ).show()
            onGuardar()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .imePadding()
    ) {
        TopHeader(title = if (editando) "Editar cliente" else "Agregar cliente", onBack = onBack)

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Navy)
            }
            return@Column
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ───────── 1. Datos personales ─────────
            TarjetaSeccion(numero = 1, titulo = "Datos personales") {
                CampoForm(
                    CampoCliente.NOMBRE, "Nombre(s)", "Ej. María Fernanda",
                    viewModel, state.form.nombre, state.errores[CampoCliente.NOMBRE]
                )
                CampoForm(
                    CampoCliente.APELLIDO_PATERNO, "Apellido paterno", "Ej. López",
                    viewModel, state.form.apellidoPaterno, state.errores[CampoCliente.APELLIDO_PATERNO]
                )
                CampoForm(
                    CampoCliente.APELLIDO_MATERNO, "Apellido materno", "Ej. Soto",
                    viewModel, state.form.apellidoMaterno, state.errores[CampoCliente.APELLIDO_MATERNO]
                )
            }

            // ───────── 2. Contacto ─────────
            TarjetaSeccion(numero = 2, titulo = "Contacto") {
                CampoForm(
                    CampoCliente.TELEFONO, "Teléfono", "477 000 0000",
                    viewModel, state.form.telefono, state.errores[CampoCliente.TELEFONO],
                    teclado = KeyboardType.Phone, icono = Icons.Filled.Phone
                )
                CampoForm(
                    CampoCliente.CORREO, "Correo electrónico", "correo@ejemplo.com",
                    viewModel, state.form.correo, state.errores[CampoCliente.CORREO],
                    teclado = KeyboardType.Email, icono = Icons.Filled.Email
                )
            }

            // ───────── 3. Dirección ─────────
            TarjetaSeccion(numero = 3, titulo = "Dirección") {
                CampoForm(
                    CampoCliente.CALLE, "Calle", "Nombre de la calle",
                    viewModel, state.form.calle, state.errores[CampoCliente.CALLE]
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoForm(
                        CampoCliente.NUM_EXTERIOR, "Núm. exterior", "123",
                        viewModel, state.form.numExterior, state.errores[CampoCliente.NUM_EXTERIOR],
                        modifier = Modifier.weight(1f)
                    )
                    CampoForm(
                        CampoCliente.CODIGO_POSTAL, "C.P.", "37000",
                        viewModel, state.form.codigoPostal, state.errores[CampoCliente.CODIGO_POSTAL],
                        teclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                CampoForm(
                    CampoCliente.COLONIA, "Colonia", "Nombre de la colonia",
                    viewModel, state.form.colonia, state.errores[CampoCliente.COLONIA]
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoForm(
                        CampoCliente.CIUDAD, "Ciudad", "León",
                        viewModel, state.form.ciudad, state.errores[CampoCliente.CIUDAD],
                        modifier = Modifier.weight(1f)
                    )
                    CampoForm(
                        CampoCliente.ESTADO, "Estado", "Guanajuato",
                        viewModel, state.form.estado, state.errores[CampoCliente.ESTADO],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ───────── Nota informativa ─────────
            NotaInformativa(
                "El cliente se registrará con estatus Activo. Al modificar un cliente, " +
                        "el identificador no se puede cambiar."
            )

            state.error?.let {
                Text(it, color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
        }

        // ───────── Barra inferior de acciones ─────────
        BarraAcciones(
            guardando = state.isSaving,
            textoGuardar = if (editando) "Guardar cambios" else "Guardar cliente",
            onCancelar = onBack,
            onGuardar = { viewModel.guardar(clienteId) }
        )
    }
}

/* ───────────────────────── Componentes ───────────────────────── */

@Composable
private fun TarjetaSeccion(
    numero: Int,
    titulo: String,
    contenido: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x1A0B1F4B), spotColor = Color(0x1A0B1F4B))
            .background(Color.White, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(22.dp)
                    .background(Navy, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(numero.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(10.dp))
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        contenido()
    }
}

@Composable
private fun CampoForm(
    campo: CampoCliente,
    etiqueta: String,
    placeholder: String,
    viewModel: ClienteFormViewModel,
    valor: String,
    error: String?,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    icono: ImageVector? = null
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = etiqueta.uppercase() + if (campo.obligatorio) " *" else "",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Navy
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { viewModel.onCampo(campo, it) },
            placeholder = { Text(placeholder, color = TextLight, fontSize = 14.sp) },
            leadingIcon = icono?.let {
                { Icon(it, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp)) }
            },
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Navy,
                unfocusedBorderColor = BorderSoft,
                errorBorderColor = ErrorRed,
                focusedTextColor = Navy,
                unfocusedTextColor = Navy,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = FondoCampo,
                errorContainerColor = FondoCampo
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                error,
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun NotaInformativa(texto: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(FondoInfo, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = Navy,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(16.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(texto, fontSize = 11.sp, lineHeight = 16.sp, color = TextSecondary)
    }
}

@Composable
private fun BarraAcciones(
    guardando: Boolean,
    textoGuardar: String,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancelar,
            enabled = !guardando,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, Navy),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Button(
            onClick = onGuardar,
            enabled = !guardando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Navy,
                contentColor = Color.White,
                disabledContainerColor = Navy.copy(alpha = 0.6f),
                disabledContentColor = Color.White
            ),
            modifier = Modifier
                .weight(1.6f)
                .height(48.dp)
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Guardando...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(textoGuardar, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
