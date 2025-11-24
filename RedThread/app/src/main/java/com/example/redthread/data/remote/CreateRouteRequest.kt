package com.example.redthread.data.remote

data class CreateRouteRequest(
    val nombre: String,
    val descripcion: String? = null,
    val orderIds: List<Long>,
    val totalPrice: Long? = null
)
