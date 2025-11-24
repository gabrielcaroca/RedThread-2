package com.example.redthread.data.remote.dto

data class CreateVariantRequest(
    val productId: Int,
    val sizeType: String,
    val sizeValue: String,
    val color: String,
    val sku: String,
    val priceOverride: Int? = null,
    val stock: Int = 0
)
