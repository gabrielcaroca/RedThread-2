package com.example.redthread.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.AddItemReq
import com.example.redthread.data.remote.UpdateQtyReq
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class CartViewModel(
    app: Application
) : AndroidViewModel(app) {

    data class CartItem(
        val itemId: Long? = null,
        val variantId: Long? = null,
        val productId: Int,
        val nombre: String,
        val talla: String,
        val color: String,
        val precio: String,
        val cantidad: Int = 1,
        val unitPrice: Double? = null,
        val stockAvailable: Int? = null
    )

    private val session = SessionPrefs(app.applicationContext)

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    // ==========================
    // Helpers internos
    // ==========================

    private suspend fun isLogged(): Boolean =
        session.tokenFlow.firstOrNull()?.isNotBlank() == true

    private fun recomputeCount(list: List<CartItem>) {
        _count.value = list.sumOf { it.cantidad }
    }

    private suspend fun reloadFromBackend() {
        try {
            Log.d("CartViewModel", "reloadFromBackend() -> GET /cart")
            val cart = ApiClient.orders.getCart()

            val enriched = cart.items.map { ci ->
                val variant = ApiClient.catalog.getVariant(ci.variantId)
                val product = ApiClient.catalog.getProduct(variant.productId.toInt())

                val unit = ci.unitPrice
                CartItem(
                    itemId = ci.itemId,                 // ✅ usa itemId del DTO
                    variantId = ci.variantId,
                    productId = product.id,
                    nombre = product.name,
                    talla = variant.sizeValue,
                    color = variant.color,
                    precio = formatCLP(unit.toInt()),
                    cantidad = ci.quantity,
                    unitPrice = unit,
                    stockAvailable = variant.stock
                )
            }

            _items.value = enriched
            recomputeCount(enriched)
        } catch (e: Exception) {
            Log.e("CartViewModel", "Error recargando carrito desde backend", e)
            // dejamos el carrito local tal cual
        }
    }

    // ==========================
    // API pública
    // ==========================

    fun refreshFromBackendIfLogged() {
        viewModelScope.launch {
            if (!isLogged()) {
                Log.d("CartViewModel", "refreshFromBackendIfLogged(): no hay token, no sincroniza")
                return@launch
            }
            reloadFromBackend()
        }
    }

    fun addToCart(draft: CartItem) {
        viewModelScope.launch {
            // 1) Actualizar carrito local respetando stock
            _items.update { current ->
                val idx = current.indexOfFirst {
                    it.productId == draft.productId &&
                            it.talla.equals(draft.talla, true) &&
                            it.color.equals(draft.color, true)
                }
                if (idx >= 0) {
                    val copy = current.toMutableList()
                    val old = copy[idx]

                    val maxStock = old.stockAvailable ?: draft.stockAvailable ?: Int.MAX_VALUE
                    val desired = old.cantidad + draft.cantidad
                    val newQty = desired.coerceAtMost(maxStock)

                    copy[idx] = old.copy(cantidad = newQty)
                    copy
                } else {
                    val maxStock = draft.stockAvailable ?: Int.MAX_VALUE
                    val newQty = draft.cantidad.coerceAtMost(maxStock)
                    current + draft.copy(cantidad = newQty)
                }
            }
            recomputeCount(_items.value)

            if (!isLogged()) return@launch

            try {
                val variantId = draft.variantId
                if (variantId == null) {
                    Log.w("CartViewModel", "addToCart(): draft sin variantId, no sincroniza backend")
                    return@launch
                }

                val qty = draft.cantidad.coerceAtLeast(1)
                ApiClient.orders.addItem(
                    AddItemReq(
                        variantId = variantId,
                        quantity = qty
                    )
                )

                reloadFromBackend()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error agregando item en backend", e)
            }
        }
    }

    fun updateQty(item: CartItem, newQty: Int) {
        viewModelScope.launch {
            val safeQty = newQty.coerceAtLeast(1)

            _items.update { list ->
                list.map {
                    if (it == item) it.copy(cantidad = safeQty) else it
                }
            }
            recomputeCount(_items.value)

            if (!isLogged() || item.itemId == null) return@launch

            try {
                ApiClient.orders.updateItem(item.itemId, UpdateQtyReq(safeQty))
                reloadFromBackend()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error actualizando cantidad en backend", e)
            }
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            _items.update { list -> list.filterNot { it == item } }
            recomputeCount(_items.value)

            if (!isLogged() || item.itemId == null) return@launch

            try {
                ApiClient.orders.deleteItem(item.itemId)
                reloadFromBackend()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error eliminando item en backend", e)
            }
        }
    }

    fun clearLocal() {
        _items.value = emptyList()
        _count.value = 0
    }

    /**
     * Vaciar carrito local + backend.
     * Esto es lo que llama CarroScreen con vm.clear()
     */
    fun clear() {
        viewModelScope.launch {
            _items.value = emptyList()
            _count.value = 0

            if (!isLogged()) return@launch

            try {
                ApiClient.orders.clearCart()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error limpiando carrito en backend", e)
            }
        }
    }

    // ==========================
    // Utilidades
    // ==========================

    private fun formatCLP(n: Int): String {
        val s = String.format(Locale.US, "%,d", n)
        return "$" + s.replace(',', '.')
    }
}
