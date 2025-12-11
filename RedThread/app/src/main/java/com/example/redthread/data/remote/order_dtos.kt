package com.example.redthread.data.remote

/**
 * DTOs que mapean 1:1 lo que expone el order-service.
 */

// ====== CARRITO ======

data class CartItemRes(
    // IMPORTANTE: mismos nombres que el backend
    val itemId: Long,
    val variantId: Long,
    val quantity: Int,
    val unitPrice: Double
)

data class CartRes(
    // IMPORTANTE: mismos nombres que el backend
    val cartId: Long,
    val items: List<CartItemRes>,
    val total: Double
)

/**
 * Request para agregar un item al carrito.
 */
data class AddItemReq(
    val variantId: Long,
    val quantity: Int
)

/**
 * Request para actualizar la cantidad de un item del carrito.
 */
data class UpdateQtyReq(
    val quantity: Int
)

/**
 * Request de checkout: por ahora solo necesita la dirección elegida.
 */
data class CheckoutReq(
    val addressId: Long
)

// ====== PEDIDOS / HISTORIAL ======

data class OrderItemRes(
    val variantId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

data class OrderRes(
    val id: Long,
    val status: String,
    val totalAmount: Double,
    val items: List<OrderItemRes>
)
