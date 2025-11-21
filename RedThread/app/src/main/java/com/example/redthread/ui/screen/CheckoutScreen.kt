package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import com.example.redthread.ui.viewmodel.PedidoViewModel
import com.example.redthread.ui.viewmodel.ProfileViewModel

@Composable
fun CheckoutScreen(
    cartVm: CartViewModel,
    onGoPerfil: () -> Unit,
    onPaidSuccess: (pedidoId: Long, totalSnapshot: Int, metodo: MetodoPago) -> Unit
) {
    val profileVm: ProfileViewModel = viewModel()
    val pedidoVm: PedidoViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val profileState by profileVm.state.collectAsState()
    val items = cartVm.items.collectAsState().value

    // Totales: subtotal (sin IVA), IVA 19%, total
    val subtotal = items.sumOf { parsePriceToInt(it.precio) * it.cantidad }
    val iva = (subtotal * 0.19).toInt()
    val total = subtotal + iva

    // Dirección
    val direcciones = profileState.addresses
    var direccionSeleccionadaId by remember {
        mutableStateOf(direcciones.firstOrNull { it.predeterminada }?.id ?: direcciones.firstOrNull()?.id)
    }

    // Método de pago
    var metodo by remember { mutableStateOf(MetodoPago.DEBITO) }

    // UI state
    var isPaying by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Checkout", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // === Resumen ===
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.06f))) {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = TextSecondary)
                    Text(formatCLP(subtotal), color = TextPrimary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("IVA (19%)", color = TextSecondary)
                    Text(formatCLP(iva), color = TextPrimary)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TextSecondary.copy(alpha = 0.2f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(formatCLP(total), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === Dirección de entrega ===
        Text("Dirección de entrega", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (direcciones.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("No tienes direcciones registradas.", color = TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("Agrega una dirección antes de pagar.", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onGoPerfil) { Text("Agregar dirección") }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                direcciones.forEach { dir ->
                    val selected = direccionSeleccionadaId == dir.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { direccionSeleccionadaId = dir.id },
                                role = Role.RadioButton
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { direccionSeleccionadaId = dir.id }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${dir.alias} • ${dir.linea1}", color = TextPrimary)
                            val extra = listOfNotNull(dir.comuna, dir.ciudad, dir.region).joinToString(" · ")
                            Text(extra, color = TextSecondary, fontSize = 12.sp)
                        }
                        if (dir.predeterminada) {
                            AssistChip(onClick = {}, label = { Text("Predeterminada") })
                        }
                    }
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === Método de pago ===
        Text("Método de pago", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Column {
            MetodoPago.values().forEach { m ->
                val selected = metodo == m
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            onClick = { metodo = m },
                            role = Role.RadioButton
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = { metodo = m })
                    Spacer(Modifier.width(8.dp))
                    Text(if (m == MetodoPago.DEBITO) "Débito" else "Crédito", color = TextPrimary)
                }
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
            }
        }

        Spacer(Modifier.height(20.dp))

        // === Pagar ===
        Button(
            onClick = {
                errorMsg = null
                if (items.isEmpty()) { errorMsg = "Tu carrito está vacío."; return@Button }
                if (direcciones.isEmpty() || direccionSeleccionadaId == null) {
                    errorMsg = "Selecciona o registra una dirección."; return@Button
                }
                val user = profileState.user ?: run { errorMsg = "No hay sesión activa."; return@Button }

                // Snapshot del total para no perderlo al limpiar el carrito
                val totalSnapshot = total

                isPaying = true
                scope.launch {
                    // Crear pedido ya mismo
                    val productosSnapshot = buildString {
                        items.forEach {
                            append("- ${it.nombre} (${it.talla}/${it.color}) x${it.cantidad} = ${it.precio}\n")
                        }
                    }
                    val dir = direcciones.first { it.id == direccionSeleccionadaId }
                    val pedidoId = pedidoVm.createPedidoReturnId(
                        usuario = user.name,
                        direccion = dir.linea1,
                        total = totalSnapshot.toLong(),
                        productosSnapshot = productosSnapshot
                    )
                    // Limpiar carrito y navegar a pantalla de proceso
                    cartVm.clear()
                    isPaying = false
                    onPaidSuccess(pedidoId, totalSnapshot, metodo)
                }
            },
            enabled = !isPaying && items.isNotEmpty() && direcciones.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isPaying) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Preparando pago…")
                }
            } else {
                Text("Pagar ${formatCLP(total)}")
            }
        }

        if (errorMsg != null) {
            Spacer(Modifier.height(10.dp))
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(40.dp))
    }
}

/* ===== Utilidades CLP ===== */
private fun parsePriceToInt(raw: String): Int {
    val digits = raw.filter { it.isDigit() } // "$50.000" -> "50000"
    return digits.toIntOrNull() ?: 0
}
private fun formatCLP(amount: Int): String {
    val s = java.lang.String.format(java.util.Locale.US, "%,d", amount) // 50,000
    return "$" + s.replace(',', '.') // $50.000
}
