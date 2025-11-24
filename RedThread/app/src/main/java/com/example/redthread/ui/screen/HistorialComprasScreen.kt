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
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===================================================================
//    HISTORIAL DE COMPRAS (LOCAL - ROOM)
// ===================================================================
@Composable
fun HistorialComprasScreen(navController: NavHostController) {

    val pedidoVm: PedidoViewModel = viewModel()
    val pedidos by pedidoVm.pedidos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Historial de Compras",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(12.dp))

        if (pedidos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes compras.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pedidos) { pedido ->
                    Card {
                        Column(Modifier.padding(12.dp)) {

                            Text(
                                text = "Compra #${pedido.id}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // ✅ no asumimos tipo ni nulabilidad: siempre a String
                            val fechaRaw = pedido.fecha.toString()

                            Text(
                                text = "Fecha: ${formatFecha(fechaRaw)}",
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
                                    // ✅ encoding seguro con UTF-8 sin imports extra
                                    val productosStr = URLEncoder.encode(
                                        productosList.joinToString("|"),
                                        Charsets.UTF_8.name()
                                    )

                                    val fechaEncoded = URLEncoder.encode(
                                        fechaRaw,
                                        Charsets.UTF_8.name()
                                    )

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

// ===================================================================
//    UTILIDADES
// ===================================================================
// Si tu fecha ya viene en otro formato, esto solo la devuelve tal cual.
private fun formatFecha(raw: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date: Date? = input.parse(raw)
        val output = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        if (date != null) output.format(date) else raw
    } catch (_: Exception) {
        raw
    }
}
