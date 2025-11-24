package com.example.redthread.data.remote

data class CartItemRes(
    val id: Long,
    val variantId: Long,
    val quantity: Int,
    val unitPrice: Double
)

data class CartRes(
    val id: Long,
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

data class UpdateItemReq(
    val itemId: Long,
    val quantity: Int
)

data class CheckoutReq(
    val addressId: Long
)

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
