package com.example.redthread.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val scrollState = rememberScrollState()

    // Estado actual del carrito (para saber cuánto hay ya añadido)
    val cartItems by cartVm.items.collectAsStateWithLifecycle()

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

    // Cantidad YA en el carrito para esta variante
    val qtyEnCarritoParaVariante = remember(cartItems, selectedVariant?.id) {
        val vid = selectedVariant?.id
        if (vid == null) 0
        else cartItems.filter { it.variantId == vid }.sumOf { it.cantidad }
    }

    // Stock realmente disponible considerando lo que ya está en el carrito
    val stockRestante = (stockDisponible - qtyEnCarritoParaVariante).coerceAtLeast(0)

    fun agregarAlCarrito() {
        val p = product ?: return
        val v = selectedVariant ?: return

        if (stockDisponible <= 0) {
            Toast.makeText(ctx, "Sin stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Recalcular por si cambió algo justo antes del click
        val qtyEnCarrito = cartItems
            .filter { it.variantId == v.id }
            .sumOf { it.cantidad }

        val restante = (stockDisponible - qtyEnCarrito).coerceAtLeast(0)

        if (restante <= 0) {
            Toast.makeText(
                ctx,
                "Ya tienes el máximo stock en el carrito para esta talla/color.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (quantity > restante) {
            Toast.makeText(
                ctx,
                "Solo puedes agregar $restante unidades más de este producto.",
                Toast.LENGTH_SHORT
            ).show()
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
            stockAvailable = stockDisponible
        )

        cartVm.addToCart(item)
        Toast.makeText(ctx, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
        onAddedToCart()
    }

    // ==========================
    // LAYOUT
    // ==========================
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error!!, color = Color.Red)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { reload() }) { Text("Reintentar") }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        // ===== CATEGORÍA =====
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
                    onClick = {
                        selectedVariant = v
                        quantity = 1
                    },
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

            // Solo puedes aumentar hasta el stock restante (considerando carrito)
            val puedeAumentar = stockRestante > 0 && quantity < stockRestante

            OutlinedButton(
                onClick = {
                    if (quantity < stockRestante) {
                        quantity++
                    } else {
                        Toast.makeText(
                            ctx,
                            "Stock máximo alcanzado para esta talla/color (considerando el carrito).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = puedeAumentar
            ) { Text("+") }
        }

        Spacer(Modifier.height(24.dp))

        // ===== BOTÓN AGREGAR =====
        val botonHabilitado = stockDisponible > 0 && stockRestante > 0

        Button(
            onClick = { agregarAlCarrito() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Text("Agregar al carrito")
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun formatCLP(value: Int): String {
    val s = String.format(Locale.US, "%,d", value)
    return "$" + s.replace(',', '.')
}
