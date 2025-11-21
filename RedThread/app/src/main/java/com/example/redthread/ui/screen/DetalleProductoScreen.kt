package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.CardGray
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
    onAddedToCart: () -> Unit = {},   // ahora se usará para volver a Home
    cartVm: CartViewModel
) {
    val ctx = LocalContext.current
    val drawableName = when (categoria.lowercase()) {
        "polera" -> "ph_polera"
        "chaqueta" -> "ph_chaqueta"
        "pantalon" -> "ph_pantalon"
        "zapatillas" -> "ph_zapatillas"
        "accesorio" -> "ph_accesorio"
        else -> "ph_polera"
    }
    val imgId = remember(drawableName) {
        ctx.resources.getIdentifier(drawableName, "drawable", ctx.packageName)
    }

    val tallas = listOf("XS", "S", "M", "L", "XL")
    val colores = listOf("Negro", "Blanco", "Rojo", "Azul", "Gris")

    var talla by remember { mutableStateOf<String?>(null) }
    var color by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val canAdd = talla != null && color != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        // Imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardGray),
            contentAlignment = Alignment.Center
        ) {
            if (imgId != 0) {
                Image(
                    painter = painterResource(id = imgId),
                    contentDescription = nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(text = nombre, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(text = precio, color = TextSecondary, fontSize = 16.sp)

        Spacer(Modifier.height(20.dp))

        // Dropdowns
        SimpleDropdownField(
            label = "Talla",
            options = tallas,
            selected = talla,
            onSelected = { talla = it }
        )

        Spacer(Modifier.height(12.dp))

        SimpleDropdownField(
            label = "Color",
            options = colores,
            selected = color,
            onSelected = { color = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                cartVm.addToCart(
                    CartViewModel.CartItem(
                        productId = id,
                        nombre = nombre,
                        talla = talla!!,
                        color = color!!,
                        precio = precio,
                        cantidad = 1
                    )
                )
                // ✅ Toast de confirmación
                Toast.makeText(ctx, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                // ✅ Vuelve a inicio (se hace desde NavGraph via onAddedToCart)
                scope.launch { onAddedToCart() }
            },
            enabled = canAdd,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.35f)
            )
        ) {
            Text(
                "Agregar al carrito",
                color = if (canAdd) Color.Black else Color(0xFF1A1A1A),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Dropdown estable: el click va en el contenedor; el TextField se pinta pero NO intercepta eventos. */
@Composable
private fun SimpleDropdownField(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        OutlinedTextField(
            value = selected ?: "Selecciona $label",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            isError = selected == null,
            label = { Text(label) },
            trailingIcon = {
                androidx.compose.material3.Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
