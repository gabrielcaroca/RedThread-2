package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.local.producto.ProductoEntity
import com.example.redthread.ui.viewmodel.DeveloperViewModel
import com.example.redthread.ui.viewmodel.ProductoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class DevTab { PRODUCTOS, PEDIDOS, RUTAS }

@Composable
fun DeveloperScreen(
    vm: DeveloperViewModel,
    vmProducto: ProductoViewModel
) {
    var tab by remember { mutableStateOf(DevTab.PRODUCTOS) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab.ordinal) {
            DevTab.values().forEach {
                Tab(
                    selected = tab == it,
                    onClick = { tab = it },
                    text = { Text(it.name) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (tab) {
            DevTab.PRODUCTOS -> ProductosTab(vmProducto)
            DevTab.PEDIDOS -> DespachosTab()   // sin argumentos, usa viewModel() interno
            DevTab.RUTAS -> UsuariosTab()      // sin argumentos, usa viewModel() interno
        }
    }
}

@Composable
fun ProductosTab(vmProducto: ProductoViewModel) {
    val productos by vmProducto.productos.collectAsState()
    var showNewDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showNewDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Nuevo producto")
            }
        }

        Divider()

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nombre", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
            Text("Categoría", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))
            Text("Precio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            Text("Acción", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
        }

        Divider(Modifier.padding(bottom = 6.dp))

        LazyColumn {
            items(productos.sortedBy { it.nombre.lowercase() }) { p ->
                ProductoRow(p)
                Divider()
            }
        }

        if (showNewDialog) {
            NuevoProductoDialog(onDismiss = { showNewDialog = false })
        }
    }
}

@Composable
private fun ProductoRow(p: ProductoEntity) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(p.nombre, modifier = Modifier.weight(0.4f))
        Text(p.subcategoria.lowercase(), modifier = Modifier.weight(0.25f))
        Text("$${"%,d".format(p.precio)}", modifier = Modifier.weight(0.2f))
        Row(
            modifier = Modifier.weight(0.15f),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Editar", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    if (showDialog) {
        EditarProductoDialog(producto = p, onDismiss = { showDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoProductoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).productoDao() }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("Hombre") }
    var subcategoria by remember { mutableStateOf("Polera") }
    var precio by remember { mutableStateOf("") }
    var talla by remember { mutableStateOf("M") }
    var color by remember { mutableStateOf("Negro") }
    var isFeatured by remember { mutableStateOf(false) }

    val generos = listOf("Hombre", "Mujer")
    val subcategorias = listOf("Polera", "Chaqueta", "Pantalon", "Zapatilla", "Accesorio")
    val tallas = listOf("XS", "S", "M", "L", "XL")
    val colores = listOf("Negro", "Blanco", "Rojo", "Azul", "Gris")

    var expandedGenero by remember { mutableStateOf(false) }
    var expandedSub by remember { mutableStateOf(false) }
    var expandedTalla by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val drawableName = when (subcategoria.lowercase()) {
                        "polera" -> "ph_polera"
                        "chaqueta" -> "ph_chaqueta"
                        "pantalon" -> "ph_pantalon"
                        "zapatilla" -> "ph_zapatillas"
                        "accesorio" -> "ph_accesorio"
                        else -> "ph_polera"
                    }
                    val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                    val nuevo = ProductoEntity(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = genero,
                        subcategoria = subcategoria,
                        precio = precio.toLongOrNull() ?: 0,
                        talla = talla,
                        color = color,
                        imagenRes = resId,
                        destacado = isFeatured
                    )
                    dao.upsert(nuevo)
                }
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo producto") },
        text = {
            ProductoFormulario(
                nombre = nombre, onNombreChange = { nombre = it },
                descripcion = descripcion, onDescripcionChange = { descripcion = it },
                genero = genero, onGeneroChange = { genero = it },
                subcategoria = subcategoria, onSubcategoriaChange = { subcategoria = it },
                precio = precio, onPrecioChange = { precio = it },
                talla = talla, onTallaChange = { talla = it },
                color = color, onColorChange = { color = it },
                isFeatured = isFeatured, onFeaturedChange = { isFeatured = it },
                generos = generos, subcategorias = subcategorias,
                tallas = tallas, colores = colores,
                expandedGenero = expandedGenero, onExpandedGeneroChange = { expandedGenero = it },
                expandedSub = expandedSub, onExpandedSubChange = { expandedSub = it },
                expandedTalla = expandedTalla, onExpandedTallaChange = { expandedTalla = it },
                expandedColor = expandedColor, onExpandedColorChange = { expandedColor = it }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarProductoDialog(producto: ProductoEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).productoDao() }

    var nombre by remember { mutableStateOf(producto.nombre) }
    var descripcion by remember { mutableStateOf(producto.descripcion) }
    var genero by remember { mutableStateOf(producto.categoria) }
    var subcategoria by remember { mutableStateOf(producto.subcategoria) }
    var precio by remember { mutableStateOf(producto.precio.toString()) }
    var talla by remember { mutableStateOf(producto.talla) }
    var color by remember { mutableStateOf(producto.color) }
    var isFeatured by remember { mutableStateOf(producto.destacado) }

    val generos = listOf("Hombre", "Mujer")
    val subcategorias = listOf("Polera", "Chaqueta", "Pantalon", "Zapatilla", "Accesorio")
    val tallas = listOf("XS", "S", "M", "L", "XL")
    val colores = listOf("Negro", "Blanco", "Rojo", "Azul", "Gris")

    var expandedGenero by remember { mutableStateOf(false) }
    var expandedSub by remember { mutableStateOf(false) }
    var expandedTalla by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val drawableName = when (subcategoria.lowercase()) {
                        "polera" -> "ph_polera"
                        "chaqueta" -> "ph_chaqueta"
                        "pantalon" -> "ph_pantalon"
                        "zapatilla" -> "ph_zapatillas"
                        "accesorio" -> "ph_accesorio"
                        else -> "ph_polera"
                    }
                    val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

                    val actualizado = producto.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = genero,
                        subcategoria = subcategoria,
                        precio = precio.toLongOrNull() ?: producto.precio,
                        talla = talla,
                        color = color,
                        imagenRes = resId,
                        destacado = isFeatured
                    )
                    dao.upsert(actualizado)
                }
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Editar producto") },
        text = {
            ProductoFormulario(
                nombre = nombre, onNombreChange = { nombre = it },
                descripcion = descripcion, onDescripcionChange = { descripcion = it },
                genero = genero, onGeneroChange = { genero = it },
                subcategoria = subcategoria, onSubcategoriaChange = { subcategoria = it },
                precio = precio, onPrecioChange = { precio = it },
                talla = talla, onTallaChange = { talla = it },
                color = color, onColorChange = { color = it },
                isFeatured = isFeatured, onFeaturedChange = { isFeatured = it },
                generos = generos, subcategorias = subcategorias,
                tallas = tallas, colores = colores,
                expandedGenero = expandedGenero, onExpandedGeneroChange = { expandedGenero = it },
                expandedSub = expandedSub, onExpandedSubChange = { expandedSub = it },
                expandedTalla = expandedTalla, onExpandedTallaChange = { expandedTalla = it },
                expandedColor = expandedColor, onExpandedColorChange = { expandedColor = it }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductoFormulario(
    nombre: String, onNombreChange: (String) -> Unit,
    descripcion: String, onDescripcionChange: (String) -> Unit,
    genero: String, onGeneroChange: (String) -> Unit,
    subcategoria: String, onSubcategoriaChange: (String) -> Unit,
    precio: String, onPrecioChange: (String) -> Unit,
    talla: String, onTallaChange: (String) -> Unit,
    color: String, onColorChange: (String) -> Unit,
    isFeatured: Boolean, onFeaturedChange: (Boolean) -> Unit,
    generos: List<String>, subcategorias: List<String>, tallas: List<String>, colores: List<String>,
    expandedGenero: Boolean, onExpandedGeneroChange: (Boolean) -> Unit,
    expandedSub: Boolean, onExpandedSubChange: (Boolean) -> Unit,
    expandedTalla: Boolean, onExpandedTallaChange: (Boolean) -> Unit,
    expandedColor: Boolean, onExpandedColorChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = nombre, onValueChange = onNombreChange, label = { Text("Nombre") })
        OutlinedTextField(value = descripcion, onValueChange = onDescripcionChange, label = { Text("Descripción") })

        // Género
        ExposedDropdownMenuBox(
            expanded = expandedGenero,
            onExpandedChange = { onExpandedGeneroChange(!expandedGenero) }
        ) {
            OutlinedTextField(
                value = genero,
                onValueChange = {},
                label = { Text("Género") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedGenero, onDismissRequest = { onExpandedGeneroChange(false) }) {
                generos.forEach { g ->
                    DropdownMenuItem(text = { Text(g) }, onClick = {
                        onGeneroChange(g)
                        onExpandedGeneroChange(false)
                    })
                }
            }
        }

        // Subcategoría
        ExposedDropdownMenuBox(
            expanded = expandedSub,
            onExpandedChange = { onExpandedSubChange(!expandedSub) }
        ) {
            OutlinedTextField(
                value = subcategoria,
                onValueChange = {},
                label = { Text("Subcategoría") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedSub, onDismissRequest = { onExpandedSubChange(false) }) {
                subcategorias.forEach { s ->
                    DropdownMenuItem(text = { Text(s) }, onClick = {
                        onSubcategoriaChange(s)
                        onExpandedSubChange(false)
                    })
                }
            }
        }

        // Precio
        OutlinedTextField(
            value = precio,
            onValueChange = { onPrecioChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Precio (sin puntos)") },
            singleLine = true
        )

        // Talla
        ExposedDropdownMenuBox(
            expanded = expandedTalla,
            onExpandedChange = { onExpandedTallaChange(!expandedTalla) }
        ) {
            OutlinedTextField(
                value = talla,
                onValueChange = {},
                label = { Text("Talla") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTalla) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedTalla, onDismissRequest = { onExpandedTallaChange(false) }) {
                tallas.forEach { t ->
                    DropdownMenuItem(text = { Text(t) }, onClick = {
                        onTallaChange(t)
                        onExpandedTallaChange(false)
                    })
                }
            }
        }

        // Color
        ExposedDropdownMenuBox(
            expanded = expandedColor,
            onExpandedChange = { onExpandedColorChange(!expandedColor) }
        ) {
            OutlinedTextField(
                value = color,
                onValueChange = {},
                label = { Text("Color") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedColor, onDismissRequest = { onExpandedColorChange(false) }) {
                colores.forEach { col ->
                    DropdownMenuItem(text = { Text(col) }, onClick = {
                        onColorChange(col)
                        onExpandedColorChange(false)
                    })
                }
            }
        }

        // Featured
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isFeatured, onCheckedChange = onFeaturedChange)
            Text("Marcar como Destacado")
        }
    }
}

// ------------------------- PEDIDOS -------------------------
@Composable
fun DespachosTab(
    vmPedido: com.example.redthread.ui.viewmodel.PedidoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    vmRuta: com.example.redthread.ui.viewmodel.RutaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // 🔹 Observa todos los pedidos desde el ViewModel
    val pedidos by vmPedido.pedidos.collectAsState()

    // 🔹 Filtra solo los pedidos que están pendientes
    val pedidosPendientes = pedidos.filter { it.estado == "pendiente" }

    // 🔹 Lista de IDs seleccionados por el usuario
    val seleccionados = remember { mutableStateListOf<Long>() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pedidos disponibles", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        // 🔸 Botón para crear una ruta con los pedidos seleccionados
        Button(
            onClick = {
                if (seleccionados.isNotEmpty()) {
                    val nombreRuta = "Ruta${System.currentTimeMillis() % 1000}"

                    // 1️⃣ Crear la ruta con los pedidos seleccionados
                    vmRuta.crearRuta(nombreRuta, seleccionados)

                    // 2️⃣ Marcar los pedidos como "asignados"
                    seleccionados.forEach { idPedido ->
                        vmPedido.actualizarEstadoPedido(idPedido, "asignado")
                    }

                    // 3️⃣ Limpiar la selección
                    seleccionados.clear()
                }
            },
            enabled = seleccionados.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Crear ruta con seleccionados")
        }

        Spacer(Modifier.height(12.dp))

        // 🔸 Mostrar pedidos pendientes o mensaje vacío
        if (pedidosPendientes.isEmpty()) {
            Text("No hay pedidos registrados.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pedidosPendientes) { p ->
                    PedidoItem(
                        pedido = p,
                        seleccionado = seleccionados.contains(p.id),
                        onSelect = { checked ->
                            if (checked) seleccionados.add(p.id)
                            else seleccionados.remove(p.id)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun PedidoItem(pedido: com.example.redthread.data.local.pedido.PedidoEntity, seleccionado: Boolean, onSelect: (Boolean) -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = seleccionado, onCheckedChange = onSelect)
            Column(Modifier.weight(1f)) {
                Text(pedido.usuario, fontWeight = FontWeight.Bold)
                Text(pedido.direccion, style = MaterialTheme.typography.bodySmall)
                Text("Total: $${pedido.total}", style = MaterialTheme.typography.bodySmall)
            }
            val entregado = if (pedido.entregado) "Entregado" else "Pendiente"
            Text(entregado, color = if (pedido.entregado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}

// ------------------------- RUTAS -------------------------
@Composable
fun UsuariosTab(vmRuta: com.example.redthread.ui.viewmodel.RutaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val rutas by vmRuta.rutas.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Rutas activas", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (rutas.isEmpty()) {
            Text("No hay rutas creadas.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rutas) { ruta ->
                    RutaItem(ruta, vmRuta)
                }
            }
        }
    }
}

@Composable
fun RutaItem(
    ruta: com.example.redthread.data.local.ruta.RutaEntity,
    vmRuta: com.example.redthread.ui.viewmodel.RutaViewModel
) {
    var activa by remember { mutableStateOf(ruta.activa) }

    val pedidosCount = remember(ruta.pedidosIds) {
        ruta.pedidosIds.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .size
    }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(ruta.nombre, fontWeight = FontWeight.Bold)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activa")
                    Switch(
                        checked = activa,
                        onCheckedChange = {
                            activa = it
                            vmRuta.actualizarRuta(ruta.copy(activa = it))
                        }
                    )
                }

                // ✅ Botón de eliminar conservado
                TextButton(onClick = { vmRuta.eliminarRuta(ruta) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }

            Text(
                text = "Pedidos: $pedidosCount",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

