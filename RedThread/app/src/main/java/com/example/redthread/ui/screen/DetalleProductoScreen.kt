package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
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
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.VariantDto
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
    onAddedToCart: () -> Unit = {},
    cartVm: CartViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // imagen placeholder por categoria (igual que antes)
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

    // ====== VARIANTES REALES DESDE BACKEND ======
    var variants by remember { mutableStateOf<List<VariantDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        loading = true
        loadError = null
        try {
            variants = ApiClient.catalog.listVariantsByProduct(id)
        } catch (e: Exception) {
            loadError = e.message ?: "Error cargando variantes"
            variants = emptyList()
        } finally {
            loading = false
        }
    }

    // tallas/colores que existen realmente
    val tallas = variants.map { it.sizeValue }.distinct()
    val colores = variants.map { it.color }.distinct()

    var talla by remember { mutableStateOf<String?>(null) }
    var color by remember { mutableStateOf<String?>(null) }

    val canAdd = talla != null && color != null && variants.isNotEmpty() && !loading && loadError == null

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

        Spacer(Modifier.height(18.dp))

        if (loading) {
            Text("Cargando variantes...", color = TextSecondary)
            Spacer(Modifier.height(8.dp))
        }

        if (loadError != null) {
            Text("Error: $loadError", color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        if (!loading && variants.isEmpty() && loadError == null) {
            Text(
                "Este producto no tiene variantes creadas en el catálogo.",
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
        }

        // Dropdown Talla (solo si hay data)
        SimpleDropdownField(
            label = "Talla",
            options = tallas,
            selected = talla,
            enabled = tallas.isNotEmpty(),
            onSelected = { talla = it }
        )

        Spacer(Modifier.height(12.dp))

        // Dropdown Color
        SimpleDropdownField(
            label = "Color",
            options = colores,
            selected = color,
            enabled = colores.isNotEmpty(),
            onSelected = { color = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val selectedVariant = variants.firstOrNull {
                    it.sizeValue.equals(talla, true) &&
                            it.color.equals(color, true)
                }

                if (selectedVariant == null) {
                    Toast.makeText(ctx, "No existe esa combinación en el catálogo", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                cartVm.addToCart(
                    CartViewModel.CartItem(
                        productId = id,
                        nombre = nombre,
                        talla = talla!!,
                        color = color!!,
                        precio = precio,
                        cantidad = 1,
                        variantId = selectedVariant.id
                    )
                )

                Toast.makeText(ctx, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
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

@Composable
private fun SimpleDropdownField(
    label: String,
    options: List<String>,
    selected: String?,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { expanded = true }
    ) {
        OutlinedTextField(
            value = selected ?: if (enabled) "Selecciona $label" else "No disponible",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            isError = enabled && selected == null,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
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
