package com.example.redthread.data.repository

import com.example.redthread.data.remote.*
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto

class CartRepository(
    private val ordersApi: OrdersApi,
    private val catalogApi: com.example.redthread.data.remote.CatalogApi
) {

    suspend fun getCartEnriched(): List<CartUiItem> {
        val cart = ordersApi.getCart()

        return cart.items.map { it ->
            val variant: VariantDto = catalogApi.getVariant(it.variantId)
            val product: ProductDto = catalogApi.getProduct(variant.productId.toInt())

            CartUiItem(
                itemId = it.id,
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
