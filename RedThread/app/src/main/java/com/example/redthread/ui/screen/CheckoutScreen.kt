package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    cartVm: CartViewModel,
    onGoPerfil: () -> Unit,
    onPaidSuccess: (pedidoId: Long, total: Int, metodo: MetodoPago) -> Unit
) {
    val items by cartVm.items.collectAsState()
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val profileVm: ProfileViewModel = viewModel(factory = ProfileVmFactory(context))
    val pedidoVm: PedidoViewModel = viewModel()

    val profileState by profileVm.state.collectAsState()

    var direccionSeleccionadaId by remember { mutableStateOf<Long?>(null) }
    var metodo by remember { mutableStateOf(MetodoPago.DEBITO) }
    var isPaying by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { profileVm.loadAddresses() }

    val direcciones = profileState.addresses

    // Preseleccionar dirección default
    LaunchedEffect(direcciones) {
        if (direccionSeleccionadaId == null && direcciones.isNotEmpty()) {
            direccionSeleccionadaId = direcciones.find { it.default }?.id ?: direcciones.first().id
        }
    }

    // =======================================================
    // PRECIOS
    // item.precio YA VIENE CON IVA
    // Mostramos en checkout: Subtotal (sin IVA) + IVA + Total (con IVA)
    // =======================================================

    // total bruto (con IVA), usando el precio que ya traes en cada item
    val totalBruto = items.sumOf { parseInt(it.precio) * it.cantidad }

    // subtotal neto sin IVA calculado desde el bruto
    val subtotalNeto = items.sumOf { item ->
        val brutoUnidad = parseInt(item.precio)
        val netoUnidad = (brutoUnidad / 1.19).toInt() // aproximado hacia abajo
        netoUnidad * item.cantidad
    }

    val iva = totalBruto - subtotalNeto
    val total = totalBruto

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scroll)
    ) {

        // ===============================
        // TITLE
        // ===============================
        Text(
            "Checkout",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextPrimary
        )

        Spacer(Modifier.height(16.dp))

        // ===============================
        // DIRECCIÓN
        // ===============================
        Text("Dirección de envío", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        if (direcciones.isEmpty()) {
            Text("No tienes direcciones registradas.", color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGoPerfil) { Text("Ir a Perfil") }
        } else {
            direcciones.forEach { dir ->
                DirectionRadioItem(
                    dir = dir,
                    selected = direccionSeleccionadaId == dir.id,
                    onSelect = { direccionSeleccionadaId = dir.id }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===============================
        // RESUMEN DEL PEDIDO
        // ===============================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Resumen",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = TextSecondary)
                    Text(formatCLP(subtotalNeto), color = TextPrimary)
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("IVA (19%)", color = TextSecondary)
                    Text(formatCLP(iva), color = TextPrimary)
                }

                Divider(Modifier.padding(vertical = 12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        formatCLP(total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ===============================
        // MÉTODO DE PAGO
        // ===============================
        Text("Método de pago", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        MetodoPago.values().forEach { m ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .selectable(
                        selected = metodo == m,
                        onClick = { metodo = m },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (metodo == m)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = metodo == m, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        when (m) {
                            MetodoPago.DEBITO -> "Débito"
                            MetodoPago.CREDITO -> "Crédito"
                        },
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ===============================
        // BOTÓN PAGAR (COLOR QUE DESTACA)
        // ===============================
        Button(
            onClick = {
                errorMsg = null

                if (items.isEmpty()) {
                    errorMsg = "Tu carrito está vacío."
                    return@Button
                }

                if (direccionSeleccionadaId == null) {
                    errorMsg = "Debes seleccionar una dirección."
                    return@Button
                }

                if (isPaying) return@Button
                isPaying = true

                scope.launch {
                    try {
                        val order = ApiClient.orders.checkout(
                            com.example.redthread.data.remote.CheckoutReq(
                                addressId = direccionSeleccionadaId!!
                            )
                        )

                        // Guardar pedido local para administrador e historial
                        val dir = direcciones.first { it.id == direccionSeleccionadaId }
                        val direccionTexto = buildString {
                            append(dir.line1)
                            dir.line2?.let { if (it.isNotBlank()) append(", $it") }
                            append(", ${dir.city}, ${dir.state}, ${dir.country}")
                        }

                        val productosSnapshot = items.joinToString("\n") { item ->
                            "- ${item.nombre} (${item.talla} / ${item.color}) x${item.cantidad} – ${item.precio}"
                        }

                        // total es con IVA (lo que realmente paga el cliente)
                        pedidoVm.createPedido(
                            usuario = "Cliente",
                            direccion = direccionTexto,
                            total = total.toLong(),
                            productosSnapshot = productosSnapshot
                        )

                        cartVm.refreshFromBackendIfLogged()
                        isPaying = false
                        onPaidSuccess(order.id, order.totalAmount.toInt(), metodo)

                    } catch (e: Exception) {
                        isPaying = false
                        errorMsg = "Error al hacer checkout: ${e.message}"
                    }
                }
            },
            enabled = !isPaying,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,                // 🔴 Rojo / color principal
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {

            if (isPaying) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Pagar ${formatCLP(total)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        errorMsg?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

/* -----------------------------------------------------------
   COMPONENTE RADIO DE DIRECCIÓN
------------------------------------------------------------ */
@Composable
private fun DirectionRadioItem(
    dir: AddressDto,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .selectable(selected = selected, onClick = onSelect),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(dir.line1, fontWeight = FontWeight.Medium, color = TextPrimary)
                dir.line2?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

/* -----------------------------------------------------------
   UTILIDADES
------------------------------------------------------------ */
private fun parseInt(raw: String): Int =
    raw.filter { it.isDigit() }.toIntOrNull() ?: 0

private fun formatCLP(amount: Int): String {
    val s = String.format(java.util.Locale.US, "%,d", amount)
    return "$" + s.replace(",", ".")
}
