package com.example.redthread.data.remote

data class RouteDto(
    val id: Long,
    val nombre: String,
    val descripcion: String = "",
    val totalPedidos: Int = 0,
    val totalPrice: Long = 0,
    val activa: Boolean = true,
    val assignedUserId: Long? = null
)

data class ShipmentDto(
    val id: Long,
    val orderId: Long,
    val userId: Long?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val state: String?,
    val zip: String?,
    val country: String?,
    val status: String?,
    val totalPrice: Long?
)

data class NoteRequest(val note: String)
