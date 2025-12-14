package com.example.redthread.data.remote.dto


import java.math.BigDecimal

data class AdminOrderItemRes(
    val variantId: Long,
    val productName: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal
)
