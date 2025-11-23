package com.example.redthread.data.remote.Dto

data class ProductDto(
    val id: Int,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val active: Boolean,
    val images: List<ImageDto>?,
    val category: CategoryDto?,
    val brand: BrandDto?
)
