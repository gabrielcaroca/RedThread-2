package com.example.redthread.data.repository

import com.example.redthread.data.remote.CatalogApi
import com.example.redthread.data.remote.dto.*
import okhttp3.MultipartBody

class CatalogRepository(
    private val api: CatalogApi
) {

    // CATEGORÍAS
    suspend fun getCategories() = api.listCategories()

    // MARCAS
    suspend fun getBrands() = api.listBrands()

    // PRODUCTOS
    suspend fun getProducts() = api.listProducts()

    suspend fun getProduct(id: Int) = api.getProduct(id)

    suspend fun createProduct(req: CreateProductRequest) =
        api.createProduct(req)

    suspend fun updateProduct(id: Int, req: CreateProductRequest) =
        api.updateProduct(id, req)



    suspend fun deleteProduct(id: Int) =
        api.deleteProduct(id)

    // VARIANTES
    suspend fun createVariant(req: CreateVariantRequest) =
        api.createVariant(req)

    suspend fun getVariant(id: Long) = api.getVariant(id)

    suspend fun updateVariant(id: Long, req: CreateVariantRequest) =
        api.updateVariant(id.toInt(), req)

    suspend fun getVariantsByProduct(productId: Int): List<VariantDto> =
        api.getVariants(productId)


    // IMÁGENES LOCAL (archivo)
    suspend fun uploadImage(id: Int, body: MultipartBody.Part) =
        api.uploadImage(id, body)

    // IMÁGENES DESDE URL
    suspend fun uploadImageFromUrl(id: Int, url: String) =
        api.uploadImageFromUrl(id, UploadImageUrlRequest(url))
}
