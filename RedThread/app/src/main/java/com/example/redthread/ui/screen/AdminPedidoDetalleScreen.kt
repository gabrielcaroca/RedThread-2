package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.redthread.ui.viewmodel.AdminPedidoDetalleViewModel

@Composable
fun AdminPedidoDetalleScreen(
    orderId: Long,
    vm: AdminPedidoDetalleViewModel,
    onBack: () -> Unit
) {
    val order by vm.order.collectAsState()

    LaunchedEffect(orderId) {
        vm.loadOrder(orderId)
    }

    if (order == null) {
        CircularProgressIndicator()
        return
    }

    Column(Modifier.padding(16.dp)) {

        // ======================
        // PEDIDO
        // ======================
        Text(
            text = "Pedido #${order?.id ?: "-"}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        // ======================
        // CLIENTE
        // ======================
        Text(
            text = "Cliente",
            fontWeight = FontWeight.Bold
        )
        Text(order?.userEmail ?: "Cliente no disponible")

        Spacer(Modifier.height(8.dp))

        // ======================
        // DIRECCIÓN
        // ======================
        Text(
            text = "Dirección de entrega",
            fontWeight = FontWeight.Bold
        )
        Text(order?.fullAddress ?: "Dirección no disponible")

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        // ======================
        // ESTADO Y TOTAL
        // ======================
        Text("Estado: ${order?.status ?: "-"}")
        Text(
            text = "Total: $${order?.totalAmount ?: 0.0}",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // ======================
        // PRODUCTOS
        // ======================
        Text(
            text = "Productos",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        order?.items.orEmpty().forEach { item ->
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {

                Text(
                    text = item.productName ?: "Producto sin nombre",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Talla: ${item.size ?: "-"} | Color: ${item.color ?: "-"}"
                )

                Text(
                    text = "Cantidad: ${item.quantity ?: 0}"
                )

                Text(
                    text = "Precio unitario: $${item.unitPrice ?: 0.0}"
                )

                Text(
                    text = "Subtotal: $${item.lineTotal ?: 0.0}",
                    fontWeight = FontWeight.Bold
                )

                Divider(Modifier.padding(top = 8.dp))
            }
        }
    }
}
