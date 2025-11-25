package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import kotlinx.coroutines.launch

@Composable
fun DetalleProductoScreen(
    id: Int,
    nombre: String,
    precio: String,
    categoria: String,
    cartVm: CartViewModel,
    onAddedToCart: () -> Unit = {},   // ← este irá al carrito
    nav: NavHostController            // ← necesitas el nav para navegar
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // SOLO EL NOMBRE DEL PRODUCTO
        Text(
            text = nombre,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(30.dp))

        // BOTÓN COMPRAR
        Button(
            onClick = {
                // ir al carrito
                nav.navigate("carro") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Comprar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

