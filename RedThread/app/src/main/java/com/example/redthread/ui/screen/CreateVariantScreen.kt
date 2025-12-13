package com.example.redthread.ui.screen.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redthread.data.remote.dto.CreateVariantRequest
import com.example.redthread.ui.components.DropdownMenuBox
import com.example.redthread.ui.viewmodel.CatalogViewModel
import com.example.redthread.domain.validation.validateStock

@Composable
fun CreateVariantScreen(
    productId: Int,
    variantId: Long? = null,
    vm: CatalogViewModel,
    onNext: () -> Unit
) {
    val letterSizes = listOf("XXS", "XS", "S", "M", "L", "XL", "XXL")
    val euSizes = (39..46).map { it.toString() }

    val variant by vm.currentVariant.collectAsState()

    var sizeType by remember { mutableStateOf("") }
    var selectedSizeIndex by remember { mutableStateOf(-1) }
    var sizeValue by remember { mutableStateOf("") }

    var color by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var stockError by remember { mutableStateOf<String?>(null) }

    // Cargar variante si estamos editando
    LaunchedEffect(variantId) {
        if (variantId != null) vm.loadVariant(variantId)
    }

    // Precargar datos
    LaunchedEffect(variant) {
        if (variant != null) {
            sizeType = variant!!.sizeType
            sizeValue = variant!!.sizeValue
            color = variant!!.color
            stock = variant!!.stock?.toString() ?: "0"
            selectedSizeIndex =
                if (sizeType == "EU") euSizes.indexOf(sizeValue)
                else letterSizes.indexOf(sizeValue)

            sku = variant!!.sku // mantenemos SKU original
        }
    }

    fun generateSku() {
        if (sizeType.isNotEmpty() && sizeValue.isNotEmpty() && color.isNotEmpty()) {
            sku =
                "SKU-${sizeType.take(3)}-${sizeValue}-${color.take(3).uppercase()}-${System.currentTimeMillis() % 9999}"
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {

        Text(
            if (variantId == null) "Agregar variante" else "Editar variante",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        DropdownMenuBox(
            label = "Tipo de talla",
            items = listOf("EU", "LETTER"),
            selectedIndex = if (sizeType == "") -1 else listOf("EU", "LETTER").indexOf(sizeType),
            onSelect = {
                sizeType = listOf("EU", "LETTER")[it]
                selectedSizeIndex = -1
                sizeValue = ""
                generateSku()
            }
        )

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

        OutlinedTextField(
            value = stock,
            onValueChange = {
                stock = it.filter { c -> c.isDigit() }
                stockError = validateStock(stock)
            },
            label = { Text("Stock") },
            isError = stockError != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = sku,
            onValueChange = {},
            enabled = false,
            label = { Text("SKU") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {

                if (sizeType.isBlank() || sizeValue.isBlank() || color.isBlank()) return@Button

                val err = validateStock(stock)
                if (err != null) {
                    stockError = err
                    return@Button
                }

                val req = CreateVariantRequest(
                    productId = productId,
                    sizeType = sizeType,
                    sizeValue = sizeValue,
                    color = color,
                    sku = sku,
                    priceOverride = null,
                    stock = stock.toInt()
                )

                if (variantId == null) {
                    vm.createVariant(req) { onNext() }
                } else {
                    vm.updateVariant(variantId!!, req) {}
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (variantId == null) "Crear variante" else "Guardar cambios")
        }
    }
}
