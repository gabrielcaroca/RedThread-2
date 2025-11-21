package com.example.redthread.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redthread.data.local.producto.ProductoEntity
import com.example.redthread.ui.viewmodel.Categoria
import com.example.redthread.ui.viewmodel.ProductoViewModel

@Composable
fun CatalogoScreen(vm: ProductoViewModel) {
    Column(Modifier.fillMaxSize()) {
        CategoriaTabs(vm)
        ProductosGrid(vm)
    }
}

@Composable
private fun CategoriaTabs(vm: ProductoViewModel) {
    val categoria by vm.categoria.collectAsState()

    TabRow(selectedTabIndex = categoria.ordinal) {
        Categoria.values().forEach { cat ->
            Tab(
                selected = categoria == cat,
                onClick = { vm.setCategoria(cat) },
                text = { Text(cat.name.uppercase()) }
            )
        }
    }
}

@Composable
private fun ProductosGrid(vm: ProductoViewModel) {
    val productos by vm.productos.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(productos) { p ->
            ProductoCard(p)
        }
    }
}

@Composable
private fun ProductoCard(p: ProductoEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = p.imagenRes),
                contentDescription = p.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = p.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$${"%,d".format(p.precio)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
