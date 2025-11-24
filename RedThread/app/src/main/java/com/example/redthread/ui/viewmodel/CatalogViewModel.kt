package com.example.redthread.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.dto.*
import com.example.redthread.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CatalogViewModel(
    app: Application,
    private val repo: CatalogRepository
) : AndroidViewModel(app) {

    // ============================
    // ESTADOS
    // ============================
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories

    private val _brands = MutableStateFlow<List<BrandDto>>(emptyList())
    val brands: StateFlow<List<BrandDto>> = _brands

    private val _product = MutableStateFlow<ProductDto?>(null)
    val product: StateFlow<ProductDto?> = _product

    // 🔥 NUEVO → lista completa de productos del backend
    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products

    // 🔥 NUEVO → carga lista de productos
    fun loadProducts() = viewModelScope.launch {
        safeCall(
            action = { repo.getProducts() },
            onSuccess = { _products.value = it }
        )
    }

    // ============================
    // CARGAR DATOS
    // ============================
    fun loadCategories() = viewModelScope.launch {
        safeCall(
            action = { repo.getCategories() },
            onSuccess = { _categories.value = it }
        )
    }

    fun loadBrands() = viewModelScope.launch {
        safeCall(
            action = { repo.getBrands() },
            onSuccess = { _brands.value = it }
        )
    }

    fun loadProduct(id: Int) = viewModelScope.launch {
        safeCall(
            action = { repo.getProduct(id) },
            onSuccess = { _product.value = it }
        )
    }

    // ============================
    // CREAR PRODUCTO
    // ============================
    fun createProduct(req: CreateProductRequest, onCreated: (Int) -> Unit) =
        viewModelScope.launch {
            safeCall(
                action = { repo.createProduct(req) },
                onSuccess = {
                    _product.value = it
                    onCreated(it.id)
                }
            )
        }

    // ============================
    // CREAR VARIANTE
    // ============================
    fun createVariant(req: CreateVariantRequest, onDone: () -> Unit) =
        viewModelScope.launch {
            safeCall(
                action = { repo.createVariant(req) },
                onSuccess = { onDone() }
            )
        }

    // ============================
    // SUBIR IMAGEN (archivo)
    // ============================
    fun uploadImage(productId: Int, file: MultipartBody.Part, onDone: () -> Unit) =
        viewModelScope.launch {
            safeCall(
                action = { repo.uploadImage(productId, file) },
                onSuccess = { onDone() }
            )
        }

    // ============================
    // SUBIR IMAGEN DESDE URL
    // ============================
    fun uploadImageFromUrl(productId: Int, url: String, onDone: () -> Unit) =
        viewModelScope.launch {
            safeCall(
                action = { repo.uploadImageFromUrl(productId, url) },
                onSuccess = { onDone() }
            )
        }

    // ============================
    // SUBIR IMAGEN DESDE ARCHIVO LOCAL
    // ============================
    fun uploadImageFile(productId: Int, uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            val context = getApplication<Application>()

            try {
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se pudo leer el archivo")

                val bytes = input.readBytes()
                input.close()

                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                val part = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "image_${System.currentTimeMillis()}.jpg",
                    body = requestBody
                )

                safeCall(
                    action = { repo.uploadImage(productId, part) },
                    onSuccess = { onDone() }
                )

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ============================
    // HANDLER GENÉRICO DE ERRORES
    // ============================
    private suspend fun <T> safeCall(
        action: suspend () -> T,
        onSuccess: (T) -> Unit
    ) {
        try {
            _loading.value = true
            _error.value = null
            _success.value = null

            val result = action()
            onSuccess(result)
            _success.value = "OK"

        } catch (e: Exception) {
            _error.value = e.message ?: "Error desconocido"
        } finally {
            _loading.value = false
        }
    }
}
