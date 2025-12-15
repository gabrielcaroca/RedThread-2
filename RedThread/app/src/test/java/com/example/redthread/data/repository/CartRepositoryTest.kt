package com.example.redthread.data.repository

import com.example.redthread.data.remote.AddItemReq
import com.example.redthread.data.remote.CartItemRes
import com.example.redthread.data.remote.CartRes
import com.example.redthread.data.remote.CatalogApi
import com.example.redthread.data.remote.OrdersApi
import com.example.redthread.data.remote.UpdateQtyReq
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doAnswer

class CartRepositoryTest {

    private val ordersApi: OrdersApi = mock()
    private val catalogApi: CatalogApi = mock()

    private val repo = CartRepository(
        ordersApi = ordersApi,
        catalogApi = catalogApi
    )

    @Test
    fun `getCartEnriched returns items with product and variant data`() {
        runTest {
            val cart = CartRes(
                cartId = 10L,
                items = listOf(
                    CartItemRes(
                        itemId = 1L,
                        variantId = 100L,
                        quantity = 2,
                        unitPrice = 12990.0
                    )
                ),
                total = 25980.0
            )

            val variant = VariantDto(
                id = 100L,
                productId = 5L,
                sizeType = "LETTER",
                sizeValue = "M",
                color = "NEGRO",
                sku = "SKU-100",
                priceOverride = null,
                stock = null
            )

            val product = ProductDto(
                id = 5,
                name = "Polera",
                description = "Desc",
                basePrice = 12990.0,
                active = true,
                images = null,
                category = null,
                brand = null,
                featured = true,
                gender = "HOMBRE",
                variants = emptyList()
            )

            whenever(ordersApi.getCart()).thenReturn(cart)
            whenever(catalogApi.getVariant(100L)).thenReturn(variant)
            whenever(catalogApi.getProduct(5)).thenReturn(product)

            val result = repo.getCartEnriched()

            assertEquals(1, result.size)
            val item = result.first()
            assertEquals(1L, item.itemId)
            assertEquals(100L, item.variantId)
            assertEquals(5, item.productId)
            assertEquals("Polera", item.nombre)
            assertEquals("M", item.talla)
            assertEquals("NEGRO", item.color)
            assertEquals(12990.0, item.unitPrice, 0.0)
            assertEquals(2, item.cantidad)
        }
    }

    @Test
    fun `addVariant calls ordersApi addItem with correct body`() {
        runTest {
            whenever(ordersApi.addItem(any())).thenReturn(
                CartRes(cartId = 1L, items = emptyList(), total = 0.0)
            )

            repo.addVariant(variantId = 77L, qty = 3)

            val captor = argumentCaptor<AddItemReq>()
            verify(ordersApi).addItem(captor.capture())
            assertEquals(77L, captor.firstValue.variantId)
            assertEquals(3, captor.firstValue.quantity)
        }
    }

    @Test
    fun `updateItem calls ordersApi updateItem with correct params`() {
        runTest {
            whenever(ordersApi.updateItem(any(), any())).thenReturn(
                CartRes(cartId = 1L, items = emptyList(), total = 0.0)
            )

            repo.updateItem(itemId = 9L, qty = 5)

            val reqCaptor = argumentCaptor<UpdateQtyReq>()
            verify(ordersApi).updateItem(9L, reqCaptor.capture())
            assertEquals(5, reqCaptor.firstValue.quantity)
        }
    }

    @Test
    fun `deleteItem calls ordersApi deleteItem`() {
        runTest {
            // deleteItem retorna Unit, así que no se debe thenReturn(...)
            doAnswer { }.whenever(ordersApi).deleteItem(any())

            repo.deleteItem(12L)

            verify(ordersApi).deleteItem(12L)
        }
    }

    @Test
    fun `clear calls ordersApi clearCart`() {
        runTest {
            // clearCart retorna Unit, así que no se debe thenReturn(...)
            doAnswer { }.whenever(ordersApi).clearCart()

            repo.clear()

            verify(ordersApi).clearCart()
        }
    }
}
