package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redthread.ui.viewmodel.DeveloperViewModel
import com.example.redthread.ui.viewmodel.PedidoViewModel
import com.example.redthread.ui.viewmodel.RutaViewModel
import com.example.redthread.ui.viewmodel.CatalogViewModel
import com.example.redthread.data.remote.Dto.ProductDto

enum class DevTab { PRODUCTOS, PEDIDOS, RUTAS }

@Composable
fun DeveloperScreen(
    vm: DeveloperViewModel,
    catalogVm: CatalogViewModel,
    onCreateProduct: () -> Unit
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
            DevTab.PRODUCTOS -> ProductsTab(catalogVm, onCreateProduct)

            DevTab.PEDIDOS -> OrdersTab(
                vmPedido = viewModel(),
                vmRuta = viewModel()
            )

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
    onCreateProduct: () -> Unit
) {
    LaunchedEffect(Unit) {
        catalogVm.loadProducts()
    }

    val productos by catalogVm.products.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onCreateProduct,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Nuevo producto")
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nombre", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
            Text("Categoría", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))
            Text("Precio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            Text("Acción", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
        }

        Divider()

        LazyColumn {
            items(productos.sortedBy { it.name.lowercase() }) { p ->
                ProductRowRemote(p)
                Divider()
            }
        }
    }
}

@Composable
fun ProductRowRemote(p: ProductDto) {
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
            Button(
                onClick = { /* TODO editar */ },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Editar")
            }
        }
    }
}

//////////////////////////////////////////////////////////////////
// PEDIDOS
//////////////////////////////////////////////////////////////////

@Composable
fun OrdersTab(
    vmPedido: PedidoViewModel = viewModel(),
    vmRuta: RutaViewModel = viewModel()
) {
    val pedidos by vmPedido.pedidos.collectAsState()
    val pendientes = pedidos.filter { it.estado == "pendiente" }

    val seleccionados = remember { mutableStateListOf<Long>() }

    Column(Modifier.padding(16.dp)) {

        Text("Pedidos disponibles", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (seleccionados.isNotEmpty()) {
                    val nombreRuta = "Ruta${System.currentTimeMillis() % 1000}"
                    vmRuta.crearRuta(nombreRuta, seleccionados)
                    seleccionados.forEach { vmPedido.actualizarEstadoPedido(it, "asignado") }
                    seleccionados.clear()
                }
            },
            enabled = seleccionados.isNotEmpty()
        ) {
            Text("Crear ruta con seleccionados")
        }

        Spacer(Modifier.height(16.dp))

        if (pendientes.isEmpty()) {
            Text("No hay pedidos registrados.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pendientes) { p ->
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
fun PedidoItem(
    pedido: com.example.redthread.data.local.pedido.PedidoEntity,
    seleccionado: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
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
    }
}

//////////////////////////////////////////////////////////////////
// RUTAS
//////////////////////////////////////////////////////////////////

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
