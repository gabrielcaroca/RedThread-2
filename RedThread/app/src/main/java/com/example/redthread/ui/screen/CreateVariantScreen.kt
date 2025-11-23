package com.example.redthread.ui.screen.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redthread.data.remote.Dto.CreateVariantRequest
import com.example.redthread.ui.components.DropdownMenuBox
import com.example.redthread.ui.viewmodel.CatalogViewModel

@Composable
fun CreateVariantScreen(
    productId: Int,
    vm: CatalogViewModel,
    onNext: () -> Unit
) {
    val letterSizes = listOf("XXS", "XS", "S", "M", "L", "XL", "XXL")
    val euSizes = (39..46).map { it.toString() }

    var sizeType by remember { mutableStateOf("") }
    var selectedSizeIndex by remember { mutableStateOf(-1) }
    var sizeValue by remember { mutableStateOf("") }

    var color by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    fun generateSku() {
        if (sizeType.isNotEmpty() && sizeValue.isNotEmpty() && color.isNotEmpty()) {
            sku = "SKU-${sizeType.take(3)}-${sizeValue}-${color.take(3).uppercase()}-${System.currentTimeMillis() % 9999}"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("Agregar variante", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // ======================
        // TIPO DE TALLA
        // ======================
        DropdownMenuBox(
            label = "Tipo de talla",
            items = listOf("EU", "LETTER"),
            selectedIndex = if (sizeType.isBlank()) -1 else listOf("EU", "LETTER").indexOf(sizeType),
            onSelect = { idx ->
                sizeType = listOf("EU", "LETTER")[idx]
                selectedSizeIndex = -1
                sizeValue = ""
                generateSku()
            }
        )

        // ======================
        // TALLA
        // ======================
        if (sizeType.isNotEmpty()) {
            val list = if (sizeType == "EU") euSizes else letterSizes

            DropdownMenuBox(
                label = "Talla",
                items = list,
                selectedIndex = selectedSizeIndex,
                onSelect = { idx ->
                    selectedSizeIndex = idx
                    sizeValue = list[idx]
                    generateSku()
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ======================
        // COLOR
        // ======================
        OutlinedTextField(
            value = color,
            onValueChange = {
                color = it
                generateSku()
            },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // ======================
        // STOCK
        // ======================
        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it.filter { c -> c.isDigit() } },
            label = { Text("Stock inicial") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // ======================
        // SKU AUTO
        // ======================
        OutlinedTextField(
            value = sku,
            onValueChange = {},
            enabled = false,
            label = { Text("SKU (auto)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // ======================
        // BOTÓN CREAR
        // ======================
        Button(
            onClick = {
                if (sizeType.isBlank() || sizeValue.isBlank() || color.isBlank()) return@Button

                val req = CreateVariantRequest(
                    productId = productId,
                    sizeType = sizeType,
                    sizeValue = sizeValue,
                    color = color,
                    sku = sku,
                    priceOverride = null,
                    stock = stock.toIntOrNull() ?: 0
                )

                vm.createVariant(req) { onNext() }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear variante")
        }
    }
}
