package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CarroScreen(
    vm: CartViewModel,
    onGoCheckout: () -> Unit
) {
    val items by vm.items.collectAsState()

    // Total bruto (con IVA) usando unitPrice
    val totalBruto = items.sumOf { (it.unitPrice ?: 0.0) * it.cantidad }.roundToInt()

    // Subtotal neto (sin IVA) calculado desde el precio que ya incluye IVA
    val totalNeto = items.sumOf { item ->
        val unitBrutoInt = parseInt(item.precio)
        val unitNetoInt = (unitBrutoInt / 1.19).toInt()  // aprox sin IVA
        unitNetoInt * item.cantidad
    }

    val totalTextBruto = formatCLP(totalBruto)
    val totalTextNeto = formatCLP(totalNeto)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {

        Text(
            text = "Carrito",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tu carrito está vacío",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
            return@Column
        }

        // ========== LISTA DE ITEMS ==========
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                CartItemRow(
                    item = item,
                    onChangeQty = { newQty -> vm.updateQty(item, newQty) },
                    onRemove = { vm.removeItem(item) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ========== RESUMEN / TOTAL ==========
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtotal (sin IVA)",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = totalTextNeto,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total (con IVA)",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = totalTextBruto,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ========== BOTONES FINALES ==========
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { vm.clear() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text("Vaciar carrito")
            }

            Button(
                onClick = { onGoCheckout() },
                modifier = Modifier.weight(1f),
                enabled = items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            ) {
                Text("Ir a pagar")
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartViewModel.CartItem,
    onChangeQty: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val ctx = LocalContext.current
    val unit = item.unitPrice ?: 0.0
    val subtotalBruto = (unit * item.cantidad).roundToInt()

    // Calcular subtotal sin IVA por producto
    val unitBrutoInt = parseInt(item.precio)
    val unitNetoInt = (unitBrutoInt / 1.19).toInt()
    val subtotalNeto = unitNetoInt * item.cantidad

    val maxStock = item.stockAvailable ?: Int.MAX_VALUE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.nombre,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.talla} • ${item.color}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                if (item.stockAvailable != null) {
                    Text(
                        text = "En carrito: ${item.cantidad} / Stock: ${item.stockAvailable}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Precio unitario: ${item.precio}", // con IVA
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFE53935)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cantidad con límites
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val newQty = (item.cantidad - 1).coerceAtLeast(1)
                        if (newQty != item.cantidad) onChangeQty(newQty)
                    },
                    enabled = item.cantidad > 1
                ) {
                    Text("-")
                }

                Text(
                    text = item.cantidad.toString(),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                OutlinedButton(
                    onClick = {
                        if (item.cantidad < maxStock) {
                            val newQty = item.cantidad + 1
                            onChangeQty(newQty)
                        } else {
                            Toast.makeText(
                                ctx,
                                "Stock máximo alcanzado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("+")
                }
            }

            // Subtotal sin IVA por producto
            Text(
                text = "Subtotal (sin IVA): ${formatCLP(subtotalNeto)}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatCLP(value: Int): String {
    val s = String.format(Locale.US, "%,d", value)
    return "$" + s.replace(',', '.')
}

private fun parseInt(raw: String): Int =
    raw.filter { it.isDigit() }.toIntOrNull() ?: 0
