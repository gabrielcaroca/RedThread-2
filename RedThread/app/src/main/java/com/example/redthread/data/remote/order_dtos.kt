package com.example.redthread.data.remote

import com.google.gson.annotations.SerializedName

// ====== CARRITO ======

data class CartItemRes(
    // El backend manda "itemId"
    @SerializedName("itemId")
    val itemId: Long,

    val variantId: Long,
    val quantity: Int,
    val unitPrice: Double
)

data class CartRes(
    // El backend manda "cartId"
    @SerializedName("cartId")
    val cartId: Long,

    val items: List<CartItemRes>,
    val total: Double
)

data class AddItemReq(
    val variantId: Long,
    val quantity: Int
)

data class UpdateQtyReq(
    val quantity: Int
)

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
