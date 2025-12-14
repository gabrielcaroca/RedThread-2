package com.example.redthread.data.remote

import com.example.redthread.data.remote.dto.AdminOrderDetailRes
import com.example.redthread.data.remote.dto.OrderDetailDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
    suspend fun deleteItem(
        @Path("itemId") itemId: Long
    )

    @DELETE("cart/clear")
    suspend fun clearCart()


    @GET("orders/admin/{id}")
    suspend fun getAdminOrderDetail(
        @Path("id") id: Long
    ): AdminOrderDetailRes

    // ===== CHECKOUT =====

    @POST("checkout")
    suspend fun checkout(@Body req: CheckoutReq): OrderRes

    // ===== HISTORIAL =====

    @GET("orders")
    suspend fun listOrders(): List<OrderRes>

    @GET("orders/{id}")
    suspend fun orderDetail(@Path("id") id: Long): OrderRes

    @GET("orders/{id}")
    suspend fun getOrderDetail(
        @Path("id") id: Long
    ): OrderDetailDto
}
