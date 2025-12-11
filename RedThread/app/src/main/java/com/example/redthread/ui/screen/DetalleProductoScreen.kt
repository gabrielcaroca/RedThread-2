package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.ImageDto
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CartViewModel
import com.example.redthread.utils.absImage
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DetalleProductoScreen(
    id: Int,
    nombre: String,
    precio: String,
    categoria: String,
    cartVm: CartViewModel,
    onAddedToCart: () -> Unit = {},
    navController: NavHostController
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<ProductDto?>(null) }
    var variants by remember { mutableStateOf<List<VariantDto>>(emptyList()) }
    var images by remember { mutableStateOf<List<ImageDto>>(emptyList()) }
    var selectedVariant by remember { mutableStateOf<VariantDto?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // ===== CARGA BACKEND =====
    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                val p = ApiClient.catalog.getProduct(id)
                val v = ApiClient.catalog.listVariantsByProduct(id)
                val imgs = ApiClient.catalog.listImages(id)

                product = p
                variants = v
                images = imgs
                selectedVariant = v.firstOrNull()
                quantity = 1
            } catch (_: Exception) {
                error = "No se pudo cargar el producto. Intenta de nuevo."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(id) { reload() }

    val displayName = product?.name ?: nombre
    val displayCategory = product?.category?.name ?: categoria
    val displayDescription = product?.description.orEmpty()

    val selectedPrice = (selectedVariant?.priceOverride ?: product?.basePrice ?: 0.0)
    val priceText = formatCLP(selectedPrice.toInt())
    val stockDisponible = selectedVariant?.stock ?: 0

    fun agregarAlCarrito() {
        val p = product ?: return
        val v = selectedVariant ?: return

        if (stockDisponible <= 0) {
            Toast.makeText(ctx, "Sin stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val item = CartViewModel.CartItem(
            itemId = null,
            variantId = v.id,
            productId = p.id,
            nombre = p.name,
            talla = v.sizeValue,
            color = v.color,
            precio = priceText,
            cantidad = quantity,
            unitPrice = selectedPrice,
            stockAvailable = stockDisponible          // 👈 ahora pasamos el stock
        )

        cartVm.addToCart(item)
        Toast.makeText(ctx, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
        onAddedToCart()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LOADING
        if (loading) {
            CircularProgressIndicator(color = Color.White)
            return@Column
        }

        // ERROR
        if (error != null) {
            Text(error!!, color = Color.Red)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { reload() }) { Text("Reintentar") }
            return@Column
        }

        // ===== IMAGEN =====
        val mainImage = images.firstOrNull { it.primary == true } ?: images.firstOrNull()
        val imageUrl = absImage(mainImage?.publicUrl)

        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(bottom = 16.dp)
            )
        }

        // ===== NOMBRE =====
        Text(
            displayName,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // ===== CATEGORA =====
        if (displayCategory.isNotBlank()) {
            Text(displayCategory, color = TextSecondary, fontSize = 14.sp)
        }

        // ===== PRECIO =====
        Spacer(Modifier.height(16.dp))
        Text(
            priceText,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        // ===== STOCK =====
        val stockText =
            if (stockDisponible > 0) "Stock disponible: $stockDisponible"
            else "Sin stock disponible"

        Text(
            stockText,
            color = if (stockDisponible > 0) Color(0xFF22C55E) else Color(0xFFE53935),
            fontSize = 14.sp
        )

        // ===== DESCRIPCIÓN =====
        if (displayDescription.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(displayDescription, color = TextSecondary, fontSize = 14.sp)
        }

        // ===== VARIANTES =====
        if (variants.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Talla / Color", color = TextPrimary, fontSize = 14.sp)

            variants.forEach { v ->
                val selected = v == selectedVariant

                OutlinedButton(
                    onClick = { selectedVariant = v; quantity = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) Color.White else Color.Transparent,
                        contentColor = if (selected) Color.Black else Color.White
                    )
                ) {
                    Text("${v.sizeValue} · ${v.color}")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== CANTIDAD =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cantidad", color = TextPrimary, modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { if (quantity > 1) quantity-- },
                enabled = quantity > 1
            ) { Text("-") }

            Text(
                quantity.toString(),
                color = TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            OutlinedButton(
                onClick = {
                    if (quantity < stockDisponible) quantity++
                    else Toast.makeText(
                        ctx,
                        "Stock máximo alcanzado",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                enabled = stockDisponible > 0
            ) { Text("+") }
        }

        Spacer(Modifier.height(24.dp))

        // ===== BOTÓN AGREGAR =====
        Button(
            onClick = { agregarAlCarrito() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = stockDisponible > 0
        ) {
            Text("Agregar al carrito")
        }
    }
}

private fun formatCLP(value: Int): String {
    val s = String.format(Locale.US, "%,d", value)
    return "$" + s.replace(',', '.')
}
