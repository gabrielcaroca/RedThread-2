package com.example.redthread.data.remote.dto

data class OrderItemDto(
    val productId: Long,
    val variantId: Long,
    val productName: String,
    val size: String,
    val color: String,
    val qty: Int,
    val price: Double
)