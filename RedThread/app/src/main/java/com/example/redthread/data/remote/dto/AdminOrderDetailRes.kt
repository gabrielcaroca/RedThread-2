package com.example.redthread.data.remote.dto

import java.math.BigDecimal

data class AdminOrderDetailRes(
    val id: Long,
    val status: String,
    val userEmail: String,
    val fullAddress: String,
    val totalAmount: BigDecimal,
    val items: List<AdminOrderItemRes>
)
