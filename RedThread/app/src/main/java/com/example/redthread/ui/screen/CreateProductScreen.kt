package com.example.redthread.ui.screen.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.redthread.data.remote.dto.CreateProductRequest
import com.example.redthread.ui.components.DropdownMenuBox
import com.example.redthread.ui.viewmodel.CatalogViewModel

@Composable
fun CreateProductScreen(
    vm: CatalogViewModel,
    onNext: (productId: Int) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.loadCategories()
        vm.loadBrands()
    }

    val categories by vm.categories.collectAsState()
    val brands by vm.brands.collectAsState()
    val loading by vm.loading.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedCatIndex by remember { mutableStateOf(-1) }
    var selectedBrandIndex by remember { mutableStateOf(-1) }

    var basePrice by remember { mutableStateOf("") }

    // NUEVOS CAMPOS
    var featured by remember { mutableStateOf(false) }

    // Opciones del enum ProductGender
    val genderOptions = listOf("MALE", "FEMALE", "UNISEX")
    val genderLabels = listOf("Hombre", "Mujer", "Unisex")
    var selectedGenderIndex by remember { mutableStateOf(-1) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Crear producto", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenuBox(
            label = "Categoría",
            items = categories.map { it.name },
            selectedIndex = selectedCatIndex,
            onSelect = { idx -> selectedCatIndex = idx }
        )

        DropdownMenuBox(
            label = "Marca",
            items = brands.map { it.name },
            selectedIndex = selectedBrandIndex,
            onSelect = { idx -> selectedBrandIndex = idx }
        )

        OutlinedTextField(
            value = basePrice,
            onValueChange = { basePrice = it },
            label = { Text("Precio base") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ===========================
        //  SWITCH DESTACADO (FEATURED)
        // ===========================
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Destacado")
            Switch(
                checked = featured,
                onCheckedChange = { featured = it }
            )
        }

        // ===========================
        //  DROPDOWN GÉNERO
        // ===========================
        DropdownMenuBox(
            label = "Género",
            items = genderLabels,
            selectedIndex = selectedGenderIndex,
            onSelect = { idx -> selectedGenderIndex = idx }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank()
                    || selectedCatIndex == -1
                    || selectedBrandIndex == -1
                    || basePrice.isBlank()
                    || selectedGenderIndex == -1
                ) return@Button

                val req = CreateProductRequest(
                    categoryId = categories[selectedCatIndex].id,
                    brandId = brands[selectedBrandIndex].id,
                    name = name,
                    description = description,
                    basePrice = basePrice.toInt(),
                    featured = featured,
                    gender = genderOptions[selectedGenderIndex]
                )

                vm.createProduct(req) { newId ->
                    onNext(newId)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Creando..." else "Crear producto")
        }
    }
}
