package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import java.util.Locale

@Composable
fun CarroScreen(
    vm: CartViewModel,
    onGoCheckout: () -> Unit = {}
) {
    val items by vm.items.collectAsState()

    // --- Utils para CLP ---
    fun parsePriceToInt(raw: String): Int {
        // "$50.000" -> 50000
        val digits = raw.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }
    fun formatCLP(amount: Int): String {
        val s = String.format(Locale.US, "%,d", amount) // "50,000"
        return "$" + s.replace(',', '.')                // "$50.000"
    }

    val total = items.sumOf { parsePriceToInt(it.precio) * it.cantidad }
    val ivaInt = (total * 0.19).toInt()
    val subtotal = total - ivaInt


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = "Carrito", color = TextPrimary, fontSize = 20.sp)

        Spacer(Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.08f))
        Spacer(Modifier.height(12.dp))

        // Lista scrollable
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)     // <<< deja espacio para el footer fijo
        ) {
            items(items) { it ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF202020))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = it.nombre, color = TextPrimary)
                        Text(
                            text = "Talla: ${it.talla} · Color: ${it.color}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(text = it.precio, color = TextPrimary)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tu carrito está vacío", color = TextSecondary)
            }
        }

        // --- FOOTER FIJO: Subtotal + botones ---
        Spacer(Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.12f))
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Subtotal", color = TextSecondary)
            Text(formatCLP(subtotal), color = TextPrimary, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { vm.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
            ) { Text("Vaciar", color = Color.White) }

            Button(
                onClick = onGoCheckout,                                 // <<< ir a Checkout
                enabled = items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) { Text("Ir a pagar", color = Color.Black) }
        }
    }
}
