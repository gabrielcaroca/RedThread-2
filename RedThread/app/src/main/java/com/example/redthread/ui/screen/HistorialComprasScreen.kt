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
import androidx.navigation.NavHostController
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.OrderRes
import com.example.redthread.data.remote.dto.OrderDetailDto
import com.example.redthread.navigation.Route
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===================================================================
//    HISTORIAL DE COMPRAS (BACKEND - ORDER-SERVICE)
// ===================================================================
@Composable
fun HistorialComprasScreen(navController: NavHostController) {

    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var orders by remember { mutableStateOf<List<OrderRes>>(emptyList()) }

    // Cache de detalle por id para poder mostrar productos + navegar con info real
    val detailCache = remember { mutableStateMapOf<Long, OrderDetailDto>() }

    var openingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        errorMsg = null
        try {
            // ✅ GET /orders (filtrado por usuario en el backend)
            orders = ApiClient.orders.listOrders()
        } catch (_: Exception) {
            errorMsg = "No se pudo cargar el historial."
        } finally {
            loading = false
        }
    }

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

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        errorMsg?.let { msg ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(msg, color = TextSecondary)
            }
            return
        }

        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes compras.", color = TextSecondary)
            }
        } else {
            // ✅ Para que use todo el espacio disponible
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->

                    // Precarga silenciosa del detalle para mostrar productos sin romper UI
                    LaunchedEffect(order.id) {
                        if (!detailCache.containsKey(order.id)) {
                            try {
                                val detail = ApiClient.orders.getOrderDetail(order.id)
                                detailCache[order.id] = detail
                            } catch (_: Exception) {
                                // si falla, igual mostramos la card base
                            }
                        }
                    }

                    val detail = detailCache[order.id]

                    Card {
                        Column(Modifier.padding(12.dp)) {

                            Text(
                                text = "Compra #${order.id}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // En tu backend actual no estás enviando fecha aquí (tu DetalleCompra igual la muestra vacía).
                            val fechaRaw = ""

                            Text(
                                text = "Fecha: ${formatFecha(fechaRaw)}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // Total real: preferimos el del detalle (OrderDetailDto.total) si ya cargó
                            val totalValue: Long = if (detail != null) {
                                detail.total.toLong()
                            } else {
                                order.totalAmount.toLong()
                            }

                            Text(
                                "Total: $${"%,d".format(totalValue)}",
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Productos:",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            val productosList: List<String> =
                                if (detail != null && detail.items.isNotEmpty()) {
                                    detail.items.map { item ->
                                        val price = item.price.toLong()
                                        "${item.productName} (${item.size} / ${item.color}) x${item.qty} – $${"%,d".format(price)}"
                                    }
                                } else {
                                    listOf("Cargando productos...")
                                }

                            productosList.forEach { p ->
                                Text("• $p", color = TextSecondary)
                            }

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (openingId != null) return@Button
                                    openingId = order.id

                                    scope.launch {
                                        try {
                                            // Aseguramos detalle antes de navegar
                                            val d = detailCache[order.id] ?: ApiClient.orders.getOrderDetail(order.id).also {
                                                detailCache[order.id] = it
                                            }

                                            val productosStr = URLEncoder.encode(
                                                d.items.map { item ->
                                                    val price = item.price.toLong()
                                                    "${item.productName} (${item.size} / ${item.color}) x${item.qty} – $${"%,d".format(price)}"
                                                }.joinToString("|"),
                                                Charsets.UTF_8.name()
                                            )

                                            val fechaEncoded = URLEncoder.encode(
                                                fechaRaw,
                                                Charsets.UTF_8.name()
                                            )

                                            navController.navigate(
                                                "${Route.DetalleCompra.path}/${d.id}/$fechaEncoded/${d.total.toLong()}/$productosStr"
                                            )
                                        } catch (_: Exception) {
                                            errorMsg = "No se pudo abrir el detalle de la compra."
                                        } finally {
                                            openingId = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.align(Alignment.End),
                                enabled = openingId == null
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
    if (raw.isBlank()) return ""
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date: Date? = input.parse(raw)
        val output = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        if (date != null) output.format(date) else raw
    } catch (_: Exception) {
        raw
    }
}
