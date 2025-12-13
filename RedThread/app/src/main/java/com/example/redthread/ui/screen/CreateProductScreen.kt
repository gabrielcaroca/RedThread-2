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
import com.example.redthread.domain.validation.validatePrice

@Composable
fun CreateProductScreen(
    vm: CatalogViewModel,
    productId: Int? = null,
    onNext: (productId: Int) -> Unit
) {
    // Observamos estados del ViewModel
    val categories by vm.categories.collectAsState()
    val brands by vm.brands.collectAsState()
    val loading by vm.loading.collectAsState()
    val product by vm.currentProduct.collectAsState()

    // Estados locales
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }

    var selectedCatIndex by remember { mutableStateOf(-1) }
    var selectedBrandIndex by remember { mutableStateOf(-1) }

    var featured by remember { mutableStateOf(false) }

    val genderOptions = listOf("HOMBRE", "MUJER")
    val genderLabels = listOf("Hombre", "Mujer")
    var selectedGenderIndex by remember { mutableStateOf(-1) }

    // Cargar categorías y marcas al abrir
    LaunchedEffect(Unit) {
        vm.loadCategories()
        vm.loadBrands()
    }

    // Cargar el producto si es edición
    LaunchedEffect(productId) {
        if (productId != null) vm.loadProduct(productId)
    }

    // Cuando llega el producto -> precargar datos en los TextField
    LaunchedEffect(product, categories, brands) {
        if (product != null) {
            name = product!!.name
            description = product!!.description ?: ""

            // ProductDto tiene basePrice: Double -> lo mostramos como Int
            basePrice = product!!.basePrice.toInt().toString()

            selectedCatIndex = categories.indexOfFirst { it.id == product!!.category?.id }
            selectedBrandIndex = brands.indexOfFirst { it.id == product!!.brand?.id }

            selectedGenderIndex =
                if (product!!.gender == "HOMBRE") 0 else 1

            featured = product!!.featured
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = if (productId == null) "Crear producto" else "Editar producto",
            style = MaterialTheme.typography.headlineSmall
        )
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
            onSelect = { selectedCatIndex = it }
        )

        DropdownMenuBox(
            label = "Marca",
            items = brands.map { it.name },
            selectedIndex = selectedBrandIndex,
            onSelect = { selectedBrandIndex = it }
        )

        OutlinedTextField(
            value = basePrice,
            onValueChange = {
                basePrice = it
                priceError = validatePrice(it)  // usa tu validador
            },
            label = { Text("Precio base") },
            isError = priceError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (priceError != null) {
            Text(
                text = priceError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

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

        DropdownMenuBox(
            label = "Género",
            items = genderLabels,
            selectedIndex = selectedGenderIndex,
            onSelect = { selectedGenderIndex = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // Validaciones básicas
                if (name.isBlank()
                    || selectedCatIndex == -1
                    || selectedBrandIndex == -1
                    || basePrice.isBlank()
                    || selectedGenderIndex == -1
                ) return@Button

                priceError = validatePrice(basePrice)
                if (priceError != null) return@Button

                // Aquí YA sabemos que es Int válido y > 0
                val priceInt = basePrice.toInt()

                val req = CreateProductRequest(
                    categoryId = categories[selectedCatIndex].id,
                    brandId = brands[selectedBrandIndex].id,
                    name = name,
                    description = description,
                    basePrice = priceInt,   // <- Int, coincide con CreateProductRequest
                    featured = featured,
                    gender = genderOptions[selectedGenderIndex]
                )

                if (productId == null) {
                    // Crear: necesitamos el id nuevo, así que dejamos el callback
                    vm.createProduct(req) { newId -> onNext(newId) }
                } else {
                    // Editar: disparamos el update y navegamos de inmediato
                    vm.updateProduct(productId, req) { /* opcional: podrías refrescar algo aquí */ }
                    onNext(productId)
                }

            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(
                text = when {
                    loading -> "Procesando..."
                    productId == null -> "Crear producto"
                    else -> "Guardar cambios"
                }
            )
        }
    }
}
