package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.redthread.data.local.pedido.PedidoEntity
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.OrderRes
import com.example.redthread.ui.viewmodel.DeveloperViewModel
import com.example.redthread.ui.viewmodel.PedidoViewModel
import com.example.redthread.ui.viewmodel.RutaViewModel
import com.example.redthread.ui.viewmodel.CatalogViewModel
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto
import com.example.redthread.navigation.Route
import com.example.redthread.ui.viewmodel.AdminOrdersViewModel
import com.example.redthread.ui.viewmodel.AdminOrdersVmFactory

enum class DevTab { PRODUCTOS, PEDIDOS, RUTAS }

@Composable
fun DeveloperScreen(
    vm: DeveloperViewModel,
    catalogVm: CatalogViewModel,
    navController: NavHostController,   // ← AGREGADO
    onCreateProduct: () -> Unit,
    onEditProduct: (Int) -> Unit
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
            DevTab.PRODUCTOS -> ProductsTab(
                catalogVm = catalogVm,
                navController = navController,
                onCreateProduct = onCreateProduct,
                onEditProduct = onEditProduct
            )

            DevTab.PEDIDOS -> {
                OrdersTab(
                    navController = navController
                )
            }




            DevTab.RUTAS -> UsersTab(
                vmRuta = viewModel()
            )
        }
    }
}


//////////////////////////////////////////////////////////////////
// PRODUCTOS (API REMOTA)
//////////////////////////////////////////////////////////////////



@Composable
fun ProductsTab(
    catalogVm: CatalogViewModel,
    navController: NavHostController,
    onCreateProduct: () -> Unit,
    onEditProduct: (Int) -> Unit
) {
    LaunchedEffect(Unit) { catalogVm.loadProducts() }

    val productos by catalogVm.products.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onCreateProduct) { Text("Nuevo producto") }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Nombre", modifier = Modifier.weight(0.4f))
            Text("Categoría", modifier = Modifier.weight(0.25f))
            Text("Precio", modifier = Modifier.weight(0.2f))
            Text("Acción", modifier = Modifier.weight(0.15f))
        }

        Divider()

        LazyColumn {
            items(productos.sortedBy { it.name.lowercase() }) { p ->

                ProductRowRemote(
                    p = p,
                    onEditProduct = onEditProduct,
                    onEditVariants = { productId ->
                        navController.navigate(Route.EditarVariantes.create(productId))
                    }
                )


                // ---------- VARIANTES ----------
                p.variants?.forEach { v ->
                    VariantRowRemote(
                        v = v,
                        onEditVariant = { productId, variantId ->
                            navController.navigate(
                                Route.EditVariant.create(productId, variantId)
                            )
                        }
                    )
                }

                Divider()
            }
        }
    }
}



@Composable
fun ProductRowRemote(
    p: ProductDto,
    onEditProduct: (Int) -> Unit,
    onEditVariants: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(p.name, modifier = Modifier.weight(0.4f))
        Text(p.category?.name ?: "-", modifier = Modifier.weight(0.25f))
        Text("$${"%,.0f".format(p.basePrice)}", modifier = Modifier.weight(0.2f))

        Row(
            modifier = Modifier.weight(0.15f),
            horizontalArrangement = Arrangement.End
        ) {

            // BOTÓN DE MENÚ
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }

            // MENÚ DESPLEGABLE
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar producto") },
                    onClick = {
                        expanded = false
                        onEditProduct(p.id)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Editar variantes") },
                    onClick = {
                        expanded = false
                        onEditVariants(p.id)
                    }
                )
            }
        }
    }
}



//////////////////////////////////////////////////////////////////
// PEDIDOS
//////////////////////////////////////////////////////////////////

@Composable
fun OrdersTab(
    navController: NavHostController,
    rutaVm: RutaViewModel = viewModel()
) {
    val factory = remember {
        AdminOrdersVmFactory(ApiClient.orders)
    }

    val vm: AdminOrdersViewModel = viewModel(factory = factory)

    val orders by vm.orders.collectAsState()
    val rutas by rutaVm.rutas.collectAsState()

    // 👉 pedidos seleccionados
    val selectedIds = remember { mutableStateListOf<Long>() }

    // 👉 IDs de pedidos que YA están en alguna ruta
    val usedIds = rutas
        .flatMap { it.pedidosIds.split(",") }
        .mapNotNull { it.toLongOrNull() }

    // 👉 solo pedidos CREATED que NO estén usados
    val pendientes = orders
        .filter { it.status == "CREATED" }
        .filterNot { usedIds.contains(it.id) }

    LaunchedEffect(Unit) {
        vm.loadOrders()
    }

    Column(Modifier.padding(16.dp)) {

        Text(
            text = "Pedidos disponibles",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Button(
            enabled = selectedIds.isNotEmpty(),
            onClick = {
                rutaVm.crearRuta(
                    nombre = "Ruta ${System.currentTimeMillis()}",
                    pedidosSeleccionados = selectedIds.toList()
                )
                selectedIds.clear()
            }
        ) {
            Text("Crear ruta con seleccionados")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pendientes) { o ->
                AdminOrderSelectableItem(
                    order = o,
                    selected = selectedIds.contains(o.id),
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (!selectedIds.contains(o.id)) {
                                selectedIds.add(o.id)
                            }
                        } else {
                            selectedIds.remove(o.id)
                        }
                    },
                    onDetail = {
                        navController.navigate("admin-pedido/${o.id}")
                    }
                )
            }
        }
    }
}




@Composable
fun AdminOrderSelectableItem(
    order: OrderRes,
    selected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDetail: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Checkbox(
                    checked = selected,
                    onCheckedChange = onCheckedChange
                )

                Column {
                    Text(
                        text = "Pedido #${order.id}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Total: $${order.totalAmount}")
                }
            }

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = onDetail) {
                Text("Ver detalle")
            }
        }
    }
}





@Composable
fun AdminOrderItem(
    order: OrderRes,
    onDetail: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            Text("Pedido #${order.id}", fontWeight = FontWeight.Bold)
            Text("Estado: ${order.status}")
            Text("Total: $${order.totalAmount}")

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDetail) {
                Text("Ver detalle")
            }
        }
    }
}


@Composable
fun PedidoItem(
    pedido: PedidoEntity,
    seleccionado: Boolean,
    onSelect: (Boolean) -> Unit,
    onVerDetalle: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column {

            Row(
                Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = seleccionado, onCheckedChange = onSelect)

                Column(Modifier.weight(1f)) {
                    Text(pedido.usuario, fontWeight = FontWeight.Bold)
                    Text(pedido.direccion)
                    Text("Total: $${pedido.total}")
                }
            }

            TextButton(onClick = onVerDetalle) {
                Text("Ver detalle")
            }
        }
    }
}


//////////////////////////////////////////////////////////////////
// RUTAS
//////////////////////////////////////////////////////////////////
@Composable
fun VariantRowRemote(
    v: VariantDto,
    onEditVariant: (productId: Int, variantId: Long) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = 6.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text("${v.sizeType} - ${v.sizeValue}", modifier = Modifier.weight(0.3f))
        Text(v.color, modifier = Modifier.weight(0.25f))
        Text("Stock: ${v.stock ?: 0}", modifier = Modifier.weight(0.25f))

        Button(
            onClick = { onEditVariant(v.productId.toInt(), v.id.toLong()) },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Editar")
        }
    }
}


@Composable
fun UsersTab(vmRuta: RutaViewModel = viewModel()) {
    val rutas by vmRuta.rutas.collectAsState()

    Column(Modifier.padding(16.dp)) {

        Text("Rutas activas", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (rutas.isEmpty()) {
            Text("No hay rutas creadas.")
        } else {

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rutas) { r ->
                    RutaItem(r, vmRuta)
                }
            }
        }
    }
}

@Composable
fun RutaItem(
    ruta: com.example.redthread.data.local.ruta.RutaEntity,
    vmRuta: RutaViewModel
) {
    var activa by remember { mutableStateOf(ruta.activa) }

    val pedidosCount = ruta.pedidosIds.split(",").filter { it.isNotBlank() }.size

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {

            Text(ruta.nombre, fontWeight = FontWeight.Bold)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

                TextButton(onClick = { vmRuta.eliminarRuta(ruta) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }

            Text("Pedidos: $pedidosCount")
        }
    }



}
