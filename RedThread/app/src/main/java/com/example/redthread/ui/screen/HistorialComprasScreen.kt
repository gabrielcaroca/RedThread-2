package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.redthread.navigation.Route
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.PedidoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// === Función para formatear timestamp a fecha legible ===
private fun formatFecha(raw: Any): String {
    val ms = raw.toString().toLongOrNull() ?: return raw.toString()
    val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return fmt.format(Date(ms))
}


@Composable
fun HistorialComprasScreen(
    navController: NavHostController,
    pedidoVm: PedidoViewModel = viewModel()
) {
    val pedidos by pedidoVm.pedidos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Historial de Compras",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))

        if (pedidos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no has realizado compras.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pedidos) { pedido ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            // ✅ Mostrar fecha formateada
                            Text(
                                text = "Fecha: ${formatFecha(pedido.fecha)}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                "Total: $${"%,d".format(pedido.total)}",
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Productos:",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            val productosList =
                                pedido.productos.split("\n").filter { it.isNotBlank() }
                            productosList.forEach { p ->
                                Text("• $p", color = TextSecondary)
                            }

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val productosStr = productosList
                                        .joinToString("|")
                                        .replace(" ", "%20")
                                        .replace("\n", "%0A")
                                        .replace("/", "%2F")
                                        .replace(":", "%3A")
                                        .replace(",", "%2C")

                                    val fechaEncoded = pedido.fecha.toString()
                                        .replace(" ", "%20")
                                        .replace(":", "%3A")
                                        .replace("/", "%2F")

                                    navController.navigate(
                                        "${Route.DetalleCompra.path}/${pedido.id}/$fechaEncoded/${pedido.total}/$productosStr"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver detalle")
                            }
                        }
                    }
                }
            }
        }
    }
}
