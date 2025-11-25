package com.example.redthread.data.remote.dto

data class CreateProductRequest(
    val categoryId: Int,
    val brandId: Int,
    val name: String,
    val description: String?,
    val basePrice: Int,
    val featured: Boolean,
    val gender: String
)
