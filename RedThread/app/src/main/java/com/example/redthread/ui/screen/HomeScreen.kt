package com.example.redthread.ui.screen

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.ui.theme.Black
import com.example.redthread.ui.theme.CardGray
import com.example.redthread.ui.theme.CardGrayElevated
import com.example.redthread.ui.theme.TextPrimary
import com.example.redthread.ui.theme.TextSecondary
import com.example.redthread.ui.viewmodel.CatalogViewModel
import com.example.redthread.utils.absImage
import kotlinx.coroutines.delay

// ------------------------
// Filtros de vista
// ------------------------
enum class Filtro { TODOS, HOMBRES, MUJERES }

// Modelo visual para la pantalla
data class ProductoUi(
    val id: Int,
    val nombre: String,
    val precio: String,
    val categoria: String,
    val dtoOriginal: ProductDto? = null
)

// ------------------------
// PANTALLA PRINCIPAL
// ------------------------
@Composable
fun HomeScreen(
    catalogVm: CatalogViewModel,
    onProductoClick: (ProductoUi) -> Unit = {},
    onCarritoClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {}
) {
    var filtro by remember { mutableStateOf(Filtro.TODOS) }

    // Cargar productos al entrar
    LaunchedEffect(Unit) {
        catalogVm.loadProducts()
    }

    val productosBackend by catalogVm.products.collectAsState()
    val loading by catalogVm.loading.collectAsState()

    var subcatSel by remember { mutableStateOf<String?>(null) }

    var isVisualLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(800)
        isVisualLoading = false
    }

    // Backend -> UI
    val productosMapeados = remember(productosBackend) {
        productosBackend.map { dto ->
            ProductoUi(
                id = dto.id,
                nombre = dto.name,
                precio = "$${"%,.0f".format(dto.basePrice)}",
                categoria = dto.category?.name ?: "General",
                dtoOriginal = dto
            )
        }
    }

    // Filtro por pestañas
    val baseActual = remember(productosMapeados, filtro) {
        when (filtro) {
            Filtro.TODOS -> productosMapeados
            Filtro.HOMBRES -> productosMapeados.filter {
                it.nombre.contains("Hombre", true) || it.categoria.contains("Hombre", true)
            }
            Filtro.MUJERES -> productosMapeados.filter {
                it.nombre.contains("Mujer", true) || it.categoria.contains("Mujer", true)
            }
        }
    }

    // Categorías para dropdown
    val categoriasDisponibles by remember(baseActual) {
        mutableStateOf(
            baseActual.map { it.categoria }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        )
    }

    LaunchedEffect(categoriasDisponibles) {
        if (subcatSel != null && subcatSel !in categoriasDisponibles) {
            subcatSel = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        TabsAnimated(
            selected = filtro,
            onSelect = { filtro = it }
        )

        Spacer(Modifier.height(8.dp))

        if (categoriasDisponibles.isNotEmpty()) {
            SubcategoriaFilterDropdown(
                opciones = categoriasDisponibles,
                seleccion = subcatSel,
                onChange = { nueva -> subcatSel = nueva }
            )
            Spacer(Modifier.height(8.dp))
        }

        Crossfade(
            targetState = (isVisualLoading || (loading && productosBackend.isEmpty())),
            label = "homeLoader"
        ) { isLoading ->
            if (isLoading) {
                SkeletonGrid()
            } else {
                val listaFinal = if (subcatSel == null) {
                    baseActual
                } else {
                    baseActual.filter { it.categoria.equals(subcatSel, ignoreCase = true) }
                }

                if (listaFinal.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos disponibles", color = TextSecondary)
                    }
                } else {
                    AnimatedProductGrid(
                        filtro = filtro,
                        productos = listaFinal,
                        onProductoClick = onProductoClick
                    )
                }
            }
        }
    }
}

// ------------------------
// Dropdown
// ------------------------
@Composable
private fun SubcategoriaFilterDropdown(
    opciones: List<String>,
    seleccion: String?,
    onChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CardGray)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categoría: ${seleccion ?: "Todas"}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Abrir",
                tint = TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardGrayElevated)
        ) {
            DropdownMenuItem(
                text = { Text("Todas", color = TextPrimary) },
                onClick = { onChange(null); expanded = false }
            )
            opciones.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op, color = TextPrimary) },
                    onClick = { onChange(op); expanded = false }
                )
            }
        }
    }
}

// ------------------------
// Grilla Animada
// ------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedProductGrid(
    filtro: Filtro,
    productos: List<ProductoUi>,
    onProductoClick: (ProductoUi) -> Unit
) {
    fun Filtro.idx() = ordinal

    AnimatedContent(
        targetState = filtro,
        transitionSpec = {
            val right = targetState.idx() > initialState.idx()
            val slideIn = androidx.compose.animation.slideInHorizontally(
                tween(300)
            ) { fullWidth: Int ->
                if (right) fullWidth else -fullWidth
            }
            val slideOut = androidx.compose.animation.slideOutHorizontally(
                tween(300)
            ) { fullWidth: Int ->
                if (right) -fullWidth else fullWidth
            }
            (slideIn + fadeIn()) togetherWith (slideOut + fadeOut())
        },
        label = "gridTrans"
    ) { current ->
        key(current) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos, key = { it.id }) { p ->
                    ProductCard(
                        producto = p,
                        onClick = { onProductoClick(p) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

// ------------------------
// Tarjeta de Producto
// ------------------------
@Composable
private fun ProductCard(
    producto: ProductoUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var imageLoading by remember { mutableStateOf(true) }

    // Cargar imagen primaria desde backend
    LaunchedEffect(producto.id) {
        imageLoading = true
        try {
            val imgs = ApiClient.catalog.listImages(producto.id)
            val main = imgs.firstOrNull { it.primary == true } ?: imgs.firstOrNull()
            imageUrl = absImage(main?.publicUrl)
        } catch (_: Exception) {
            imageUrl = null
        } finally {
            imageLoading = false
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(CardGrayElevated),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageLoading -> {
                    val shimmer = rememberShimmerBrush()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmer)
                    )
                }

                imageUrl != null -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Text("Sin imagen", color = Color.Gray)
                }
            }
        }

        Column(Modifier.padding(12.dp)) {
            Text(
                producto.nombre,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(producto.categoria, color = TextSecondary, fontSize = 12.sp)
            Text(
                producto.precio,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ------------------------
// Skeletons y Helpers
// ------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SkeletonGrid(skeletonCount: Int = 8) {
    val placeholders = remember { List(skeletonCount) { it } }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(placeholders) { SkeletonCard() }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = listOf(Color(0xFF2A2A2A), Color(0xFF3A3A3A), Color(0xFF2A2A2A))
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1000),
            RepeatMode.Restart
        ),
        label = "prog"
    )
    val startX = progress * 1000f
    return Brush.linearGradient(colors, Offset(startX, 0f), Offset(startX + 500f, 0f))
}

@Composable
private fun SkeletonCard() {
    val shimmer = rememberShimmerBrush()
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(shimmer)
        )
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )
        }
    }
}

@Composable
private fun TabsAnimated(selected: Filtro, onSelect: (Filtro) -> Unit) {
    val items = listOf(
        Filtro.TODOS to "Principal",
        Filtro.HOMBRES to "Hombres",
        Filtro.MUJERES to "Mujeres"
    )
    val tabWidth = 100.dp
    val idx = items.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    val offset by animateFloatAsState(idx * tabWidth.value, tween(300), label = "")

    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp)) {
            items.forEach { (f, t) ->
                Column(
                    modifier = Modifier
                        .width(tabWidth)
                        .clickable { onSelect(f) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t,
                        color = if (f == selected) TextPrimary else TextSecondary,
                        fontWeight = if (f == selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .height(3.dp)
                .width(tabWidth)
                .offset(x = offset.dp)
                .align(Alignment.BottomStart)
                .background(Color.White)
        )
    }
}

private fun Resources.safeGetIdentifier(
    name: String,
    defType: String,
    defPackage: String
): Int {
    return try {
        getIdentifier(name, defType, defPackage)
    } catch (_: Exception) {
        0
    }
}

@SuppressLint("DiscouragedApi")
private fun android.content.Context.safeDrawableId(name: String): Int {
    return resources.safeGetIdentifier(name, "drawable", packageName)
}
