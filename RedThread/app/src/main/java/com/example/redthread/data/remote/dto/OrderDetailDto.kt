package com.example.redthread.data.remote.dto

data class OrderDetailDto(
    val id: Long,
    val userEmail: String,
    val address: AddressDto,
    val total: Double,
    val items: List<OrderItemDto>
)