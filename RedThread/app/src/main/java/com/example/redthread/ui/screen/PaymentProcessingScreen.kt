package com.example.redthread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary

@Composable
fun PaymentProcessingScreen(
    pedidoId: Long,
    total: Int,
    metodo: MetodoPago,
    onFinish: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0=tx, 1=cargando, 2=ok

    LaunchedEffect(Unit) {
        step = 0
        delay(1500) // 1.5s "Realizando transacción…"
        step = 1
        delay(2300) // 2.3s "Cargando pedido…"
        step = 2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            0 -> ProcessingBlock(title = "Realizando transacción…")
            1 -> ProcessingBlock(title = "Cargando pedido…")
            else -> ConfirmationBlock(
                pedidoId = pedidoId,
                total = total,
                metodo = metodo,
                onFinish = onFinish
            )
        }
    }
}

@Composable
private fun ProcessingBlock(title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConfirmationBlock(
    pedidoId: Long,
    total: Int,
    metodo: MetodoPago,
    onFinish: () -> Unit
) {
    Card {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.Start) {
            Text("Pedido confirmado ✅", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Pedido #$pedidoId", color = TextPrimary)
            Text("Total pagado: ${formatCLP(total)}", color = TextPrimary)
            Text("Método: ${if (metodo == MetodoPago.DEBITO) "Débito" else "Crédito"}", color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                Text("Listo")
            }
        }
    }
}

private fun formatCLP(amount: Int): String {
    val s = java.lang.String.format(java.util.Locale.US, "%,d", amount)
    return "$" + s.replace(',', '.')
}
