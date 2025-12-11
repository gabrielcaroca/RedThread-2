package com.example.redthread.data.repository

import com.example.redthread.data.remote.AddItemReq
import com.example.redthread.data.remote.OrdersApi
import com.example.redthread.data.remote.UpdateQtyReq
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto
import com.example.redthread.data.remote.CatalogApi

class CartRepository(
    private val ordersApi: OrdersApi,
    private val catalogApi: CatalogApi
) {

    suspend fun getCartEnriched(): List<CartUiItem> {
        val cart = ordersApi.getCart()

        return cart.items.map { it ->
            val variant: VariantDto = catalogApi.getVariant(it.variantId)
            val product: ProductDto = catalogApi.getProduct(variant.productId.toInt())

            CartUiItem(
                itemId = it.itemId,          // ✅ ahora usa itemId
                variantId = it.variantId,
                productId = product.id,
                nombre = product.name,
                talla = variant.sizeValue,
                color = variant.color,
                unitPrice = it.unitPrice,
                cantidad = it.quantity
            )
        }
    }

    suspend fun addVariant(variantId: Long, qty: Int) =
        ordersApi.addItem(AddItemReq(variantId, qty))

    suspend fun updateItem(itemId: Long, qty: Int) =
        ordersApi.updateItem(itemId, UpdateQtyReq(qty))

    suspend fun deleteItem(itemId: Long) =
        ordersApi.deleteItem(itemId)

    suspend fun clear() =
        ordersApi.clearCart()
}

data class CartUiItem(
    val itemId: Long,
    val variantId: Long,
    val productId: Int,
    val nombre: String,
    val talla: String,
    val color: String,
    val unitPrice: Double,
    val cantidad: Int
)
