package com.example.redthread.data.remote

import retrofit2.http.*

interface OrdersApi {

    // ===== CARRITO =====
    @GET("cart")
    suspend fun getCart(): CartRes

    @POST("cart/items")
    suspend fun addItem(@Body req: AddItemReq): CartRes

    @PATCH("cart/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: Long,
        @Body req: UpdateQtyReq
    ): CartRes

    @DELETE("cart/items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: Long)

    // alias requeridos
    @POST("cart/add")
    suspend fun addAlias(@Body req: AddItemReq): CartRes

    @POST("cart/update")
    suspend fun updateAlias(@Body req: UpdateItemReq): CartRes

    @DELETE("cart/clear")
    suspend fun clearCart()

    // ===== CHECKOUT =====
    @POST("checkout")
    suspend fun checkout(@Body req: CheckoutReq): OrderRes

    // ===== HISTORIAL =====
    @GET("orders")
    suspend fun listOrders(): List<OrderRes>

    @GET("orders/{id}")
    suspend fun orderDetail(@Path("id") id: Long): OrderRes
}
