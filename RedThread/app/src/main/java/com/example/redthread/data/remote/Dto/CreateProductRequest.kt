package com.example.redthread.data.remote.Dto

data class CreateProductRequest(
    val categoryId: Int,
    val brandId: Int,
    val name: String,
    val description: String?,
    val basePrice: Int
)
