package com.example.redthread.ui.screen

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.ui.theme.*
import com.example.redthread.ui.viewmodel.ProductoViewModel
import kotlinx.coroutines.delay

// ------------------------
// filtros de vista
// ------------------------
enum class Filtro { TODOS, HOMBRES, MUJERES }

data class ProductoUi(
    val id: Int,
    val nombre: String,
    val precio: String,
    val categoria: String,
    val target: Filtro = Filtro.TODOS
)

// ------------------------
// pantalla principal
// ------------------------
@Composable
fun HomeScreen(
    onProductoClick: (ProductoUi) -> Unit = {},
    onCarritoClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    vmProducto: ProductoViewModel = viewModel()
) {
    var filtro by remember { mutableStateOf(Filtro.TODOS) }

    val productos by vmProducto.productos.collectAsState()
    val destacados by vmProducto.destacados.collectAsState()

    // ⬇️ Estado del filtro por subcategoría (null = Todas)
    var subcatSel by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    // Base de datos visible según tab actual (para calcular subcategorías disponibles)
    val baseActual = remember(productos, destacados, filtro) {
        when (filtro) {
            Filtro.TODOS -> destacados
            Filtro.HOMBRES -> productos.filter { it.categoria.equals("Hombre", ignoreCase = true) }
            Filtro.MUJERES -> productos.filter { it.categoria.equals("Mujer", ignoreCase = true) }
        }
    }

    // Subcategorías disponibles deducidas de la base visible
    val subcategoriasDisponibles by remember(baseActual) {
        mutableStateOf(
            baseActual.map { it.subcategoria }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        )
    }

    // Si cambias de tab y la subcategoría ya no existe en el nuevo set, resetea a "Todas"
    LaunchedEffect(subcategoriasDisponibles) {
        if (subcatSel != null && subcatSel !in subcategoriasDisponibles) {
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

        // ⬇️ Dropdown de subcategorías (se muestra solo si hay opciones)
        if (subcategoriasDisponibles.isNotEmpty()) {
            SubcategoriaFilterDropdown(
                opciones = subcategoriasDisponibles,
                seleccion = subcatSel,
                onChange = { nueva ->
                    subcatSel = nueva // null = Todas
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        Crossfade(targetState = isLoading, label = "homeCrossfadeLoading") { loading ->
            if (loading) {
                SkeletonGrid()
            } else {
                // 1) Tomamos la base de entidades según el tab seleccionado
                val entidadesBase = baseActual

                // 2) Aplicamos el filtro de subcategoría si corresponde
                val entidadesFiltradas = if (subcatSel == null) {
                    entidadesBase
                } else {
                    entidadesBase.filter { it.subcategoria.equals(subcatSel, ignoreCase = true) }
                }

                // 3) Mapeamos a ProductoUi
                val lista = entidadesFiltradas.map {
                    ProductoUi(
                        id = it.id,
                        nombre = it.nombre,
                        precio = "$${"%,d".format(it.precio)}",
                        categoria = it.subcategoria,
                        target = when (filtro) {
                            Filtro.TODOS -> Filtro.TODOS
                            Filtro.HOMBRES -> Filtro.HOMBRES
                            Filtro.MUJERES -> Filtro.MUJERES
                        }
                    )
                }

                AnimatedProductGrid(
                    filtro = filtro,
                    productos = lista,
                    onProductoClick = onProductoClick
                )
            }
        }
    }
}

// ------------------------
// Dropdown de Subcategoría
// ------------------------
@Composable
private fun SubcategoriaFilterDropdown(
    opciones: List<String>,
    seleccion: String?,
    onChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Caja "pill" clickeable + menú desplegable
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
                text = "Subcategoría: ${seleccion ?: "Todas"}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Abrir",
                tint = TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(CardGrayElevated)
        ) {
            // Opción "Todas"
            DropdownMenuItem(
                text = { Text("Todas", color = TextPrimary) },
                onClick = {
                    onChange(null)
                    expanded = false
                }
            )
            // Opciones de subcategoría
            opciones.forEach { sub ->
                DropdownMenuItem(
                    text = { Text(sub, color = TextPrimary) },
                    onClick = {
                        onChange(sub)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ------------------------
// grilla con animación al cambiar tab
// ------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedProductGrid(
    filtro: Filtro,
    productos: List<ProductoUi>,
    onProductoClick: (ProductoUi) -> Unit
) {
    fun Filtro.idx(): Int = when (this) {
        Filtro.TODOS -> 0
        Filtro.HOMBRES -> 1
        Filtro.MUJERES -> 2
    }

    AnimatedContent(
        targetState = filtro,
        transitionSpec = {
            val right = targetState.idx() > initialState.idx()
            val slideIn = slideInHorizontally(
                tween(300, easing = FastOutSlowInEasing)
            ) { full -> if (right) +full else -full }
            val slideOut = slideOutHorizontally(
                tween(300, easing = FastOutSlowInEasing)
            ) { full -> if (right) -full else +full }
            (slideIn + fadeIn()) togetherWith (slideOut + fadeOut())
        },
        label = "gridTransition"
    ) { current ->
        // Usa el parámetro para evitar warning de "Target state parameter is not used"
        androidx.compose.runtime.key(current) {
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
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(300, easing = FastOutSlowInEasing),
                            fadeOutSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    )
                }
            }
        }
    }
}

// ------------------------
// grilla de carga shimmer
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
        items(placeholders) { _ ->
            SkeletonCard(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(280, easing = FastOutSlowInEasing),
                    fadeOutSpec = tween(200, easing = FastOutSlowInEasing)
                )
            )
        }
    }
}

// ------------------------
// shimmer brush
// ------------------------
@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = listOf(Color(0xFF2A2A2A), Color(0xFF3A3A3A), Color(0xFF2A2A2A))
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonShift"
    )
    val startX = progress * 600f
    val endX = startX + 300f
    return Brush.linearGradient(colors, Offset(startX, 0f), Offset(endX, 0f))
}

// ------------------------
// card placeholder
// ------------------------
@Composable
private fun SkeletonCard(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmerBrush()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(shimmer)
        )
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.85f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )
        }
    }
}

// ------------------------
// tabs con animación
// ------------------------
@Composable
private fun TabsAnimated(selected: Filtro, onSelect: (Filtro) -> Unit) {
    val items = listOf(
        Filtro.TODOS to "Principal",
        Filtro.HOMBRES to "Hombres",
        Filtro.MUJERES to "Mujeres"
    )

    val tabWidth: Dp = 100.dp
    val idx = items.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    val offset by animateFloatAsState(idx * tabWidth.value, tween(350), label = "")
    val width by animateDpAsState(tabWidth, tween(350), label = "")

    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { i, (f, t) ->
                val sel = i == idx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(tabWidth)
                        .clickable { onSelect(f) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = t,
                        color = if (sel) TextPrimary else TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        Box(
            Modifier
                .padding(horizontal = 20.dp)
                .height(2.dp)
                .width(width)
                .offset(x = offset.dp)
                .background(Color.White)
                .zIndex(1f)
        )
    }
}

// ------------------------
// card de producto
// ------------------------
@Composable
private fun ProductCard(
    producto: ProductoUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val drawableName = when (producto.categoria.lowercase()) {
        "polera" -> "ph_polera"
        "chaqueta" -> "ph_chaqueta"
        "pantalon" -> "ph_pantalon"
        "zapatilla", "zapatillas" -> "ph_zapatillas"
        "accesorio" -> "ph_accesorio"
        else -> "ph_polera"
    }
    val imgId = remember(drawableName) { ctx.safeDrawableId(drawableName) }

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
            if (imgId != 0) {
                Image(
                    painter = painterResource(id = imgId),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E2E2E))
                )
            }
        }

        Column(Modifier.padding(12.dp)) {
            Text(
                producto.nombre,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(producto.precio, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ------------------------
// helpers
// ------------------------
private fun Resources.safeGetIdentifier(name: String, defType: String, defPackage: String): Int {
    return try { getIdentifier(name, defType, defPackage) } catch (_: Exception) { 0 }
}

@SuppressLint("DiscouragedApi")
private fun android.content.Context.safeDrawableId(name: String): Int {
    return resources.safeGetIdentifier(name, "drawable", packageName)
}
