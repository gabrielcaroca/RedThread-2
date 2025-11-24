package com.example.redthread.data.remote

import com.example.redthread.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface CatalogApi {

    // CATEGORÍAS
    @GET("categories")
    suspend fun listCategories(): List<CategoryDto>

    // MARCAS
    @GET("brands")
    suspend fun listBrands(): List<BrandDto>

    // PRODUCTOS
    @GET("products")
    suspend fun listProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @POST("products")
    suspend fun createProduct(@Body req: CreateProductRequest): ProductDto

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body req: CreateProductRequest
    ): ProductDto

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)

    // VARIANTES
    @GET("variants")
    suspend fun listVariantsByProduct(@Query("productId") productId: Int): List<VariantDto>

    @POST("variants")
    suspend fun createVariant(@Body req: CreateVariantRequest): VariantDto

    // IMÁGENES
    @GET("products/{id}/images")
    suspend fun listImages(@Path("id") productId: Int): List<ImageDto>

    @Multipart
    @POST("products/{id}/images/upload")
    suspend fun uploadImage(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): ImageDto

    @POST("products/{id}/images/from-url")
    suspend fun uploadImageFromUrl(
        @Path("id") id: Int,
        @Body req: UploadImageUrlRequest
    ): ImageDto

    @POST("products/{productId}/images/{imageId}/primary")
    suspend fun markPrimaryImage(
        @Path("productId") productId: Int,
        @Path("imageId") imageId: Int
    )

    @DELETE("products/{productId}/images/{imageId}")
    suspend fun deleteImage(
        @Path("productId") productId: Int,
        @Path("imageId") imageId: Int
    )
}
