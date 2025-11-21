package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetalleCompraScreen(
    idCompra: Int,
    fecha: String,
    total: Long,
    productos: List<String>,
    navController: NavHostController // ✅ agregado
) {
    val iva = (total * 0.19).toLong()
    val subtotal = total - iva

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Detalle de compra",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text("ID: $idCompra")

        val fechaLegible = try {
            val millis = fecha.toLong()
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formato.format(Date(millis))
        } catch (e: Exception) {
            fecha // Si no se puede convertir, se muestra igual
        }

        Text("Fecha: $fechaLegible")

        Divider()

        Text("Subtotal: $${"%,d".format(subtotal)}")
        Text("IVA (19%): $${"%,d".format(iva)}")
        Text("Total: $${"%,d".format(total)}", fontWeight = FontWeight.Bold)

        Divider()

        Text("Productos:", fontWeight = FontWeight.SemiBold)
        productos.forEach { producto ->
            // Si el formato del producto incluye precio al final (por ejemplo "Chaqueta Urban (L, Negra) - 29990")
            val partes = producto.split("-").map { it.trim() }
            if (partes.size == 2) {
                Text("• ${partes[0]} — $${partes[1]}")
            } else {
                Text("• $producto")
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.popBackStack() }, // ✅ vuelve al historial
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver")
        }
    }
}
