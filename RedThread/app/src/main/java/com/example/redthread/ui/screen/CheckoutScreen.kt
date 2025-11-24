package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import com.example.redthread.ui.viewmodel.PedidoViewModel
import com.example.redthread.ui.viewmodel.ProfileViewModel
import com.example.redthread.ui.viewmodel.ProfileVmFactory
import kotlinx.coroutines.delay
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

    LaunchedEffect(Unit) { profileVm.loadAddresses() }

    val subtotal = items.sumOf { parsePriceToInt(it.precio) * it.cantidad }
    val iva = (subtotal * 0.19).toInt()
    val total = subtotal + iva

    val direcciones = profileState.addresses
    var direccionSeleccionadaId by remember { mutableStateOf<Long?>(null) }

    // ✅ Tu enum real es DEBITO / CREDITO
    var metodo by remember { mutableStateOf(MetodoPago.DEBITO) }

    var isPaying by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp)
    ) {

        Text(
            "Checkout",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )

        Spacer(Modifier.height(12.dp))

        // ============================
        // DIRECCIÓN
        // ============================
        Text("Dirección de envío", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        if (direcciones.isEmpty()) {
            Text(
                "No tienes direcciones. Ve a tu perfil para agregar una.",
                color = TextSecondary
            )
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

        // ============================
        // RESUMEN
        // ============================
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.06f)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = TextSecondary)
                    Text(formatCLP(subtotal), color = TextPrimary)
                }

                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("IVA 19%", color = TextSecondary)
                    Text(formatCLP(iva), color = TextPrimary)
                }

                Divider(Modifier.padding(vertical = 8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text(formatCLP(total), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ============================
        // MÉTODO DE PAGO
        // ============================
        Text("Método de pago", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        MetodoPago.values().forEach { m ->
            val selected = metodo == m

            val label = when (m) {
                MetodoPago.DEBITO -> "Débito"
                MetodoPago.CREDITO -> "Crédito"
            }

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
                RadioButton(selected = selected, onClick = null)
                Spacer(Modifier.width(8.dp))
                Text(label, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ============================
        // BOTÓN PAGAR
        // ============================
        Button(
            onClick = {
                errorMsg = null

                if (items.isEmpty()) {
                    errorMsg = "Tu carrito está vacío."
                    return@Button
                }

                if (direcciones.isEmpty() || direccionSeleccionadaId == null) {
                    errorMsg = "Selecciona o registra una dirección."
                    return@Button
                }

                if (isPaying) return@Button
                isPaying = true

                val totalSnapshot = total

                // ✅ tu VM espera String
                val productosSnapshot = items.joinToString("\n") {
                    "${it.nombre} x${it.cantidad}"
                }

                scope.launch {
                    delay(1200) // simula pago

                    val dir = direcciones.first { it.id == direccionSeleccionadaId }

                    val pedidoId = pedidoVm.createPedidoReturnId(
                        usuario = "Usuario",
                        direccion = dir.line1,
                        total = totalSnapshot.toLong(),
                        productosSnapshot = productosSnapshot
                    )

                    cartVm.clear()
                    isPaying = false
                    onPaidSuccess(pedidoId, totalSnapshot, metodo)
                }
            },
            enabled = !isPaying,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Black)
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

        errorMsg?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

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
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .selectable(selected = selected, onClick = onSelect),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(dir.line1, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                dir.line2?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ===================================================================
//   UTILIDADES DE CLP
// ===================================================================
private fun parsePriceToInt(raw: String): Int =
    raw.filter { it.isDigit() }.toIntOrNull() ?: 0

private fun formatCLP(amount: Int): String {
    val s = String.format(java.util.Locale.US, "%,d", amount)
    return "$" + s.replace(",", ".")
}
