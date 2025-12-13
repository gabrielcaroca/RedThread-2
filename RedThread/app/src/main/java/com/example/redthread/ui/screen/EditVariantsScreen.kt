package com.example.redthread.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.redthread.ui.viewmodel.CatalogViewModel
import com.example.redthread.navigation.Route


@Composable
fun EditVariantsScreen(
    productId: Int,
    vm: CatalogViewModel,
    navController: NavController
) {
    val variants by vm.variants.collectAsState()

    LaunchedEffect(productId) {
        vm.loadVariantsByProduct(productId)
    }

    Column(Modifier.padding(24.dp)) {
        Text("Variantes del producto", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(variants) { v ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = {
                        navController.navigate(
                            Route.EditVariant.create(productId, v.id)
                        )
                    }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${v.sizeType} ${v.sizeValue}")
                        Text("Color: ${v.color}")
                        Text("Stock: ${v.stock ?: 0}")
                    }
                }
            }
        }
    }
}
