package com.example.redthread.data.remote.dto

data class VariantDto(
    val id: Long,
    val productId: Long,
    val sizeType: String,
    val sizeValue: String,
    val color: String,
    val sku: String,
    val priceOverride: Double?,
    val stock: Int? = null
)
