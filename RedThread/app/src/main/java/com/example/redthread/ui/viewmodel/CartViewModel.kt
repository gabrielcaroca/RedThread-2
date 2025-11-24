package com.example.redthread.ui.viewmodel

import android.app.Application
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

class CartViewModel(app: Application) : AndroidViewModel(app) {

    data class CartItem(
        val itemId: Long? = null,      // id del item en backend
        val variantId: Long? = null,   // id variante real
        val productId: Int,
        val nombre: String,
        val talla: String,
        val color: String,
        val precio: String,
        val cantidad: Int = 1,
        val unitPrice: Double? = null // precio unitario real backend
    )

    private val session = SessionPrefs(app)

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    private suspend fun isLogged(): Boolean =
        session.tokenFlow.firstOrNull()?.isNotBlank() == true

    private fun recomputeCount(list: List<CartItem>) {
        _count.value = list.sumOf { it.cantidad }
    }

    // ========= CARGA REMOTA =========
    suspend fun refreshFromBackendIfLogged() {
        if (!isLogged()) return

        try {
            val cart = ApiClient.orders.getCart()
            val enriched = cart.items.map { ci ->
                val variant = ApiClient.catalog.getVariant(ci.variantId)
                val product = ApiClient.catalog.getProduct(variant.productId.toInt())

                CartItem(
                    itemId = ci.id,
                    variantId = ci.variantId,
                    productId = product.id,
                    nombre = product.name,
                    talla = variant.sizeValue,
                    color = variant.color,
                    unitPrice = ci.unitPrice,
                    precio = formatCLP(ci.unitPrice.toInt()),
                    cantidad = ci.quantity
                )
            }

            _items.value = enriched
            recomputeCount(enriched)
        } catch (_: Exception) {
            // si algo falla no crashea, solo no refresca
        }
    }

    // ========= AGREGAR =========
    fun addToCart(draft: CartItem) {
        viewModelScope.launch {
            if (!isLogged()) {
                // invitado -> local memoria
                _items.update { list ->
                    val idx = list.indexOfFirst {
                        it.productId == draft.productId &&
                                it.talla.equals(draft.talla, true) &&
                                it.color.equals(draft.color, true)
                    }
                    if (idx >= 0) {
                        val copy = list.toMutableList()
                        val old = copy[idx]
                        copy[idx] = old.copy(cantidad = old.cantidad + draft.cantidad)
                        copy
                    } else list + draft
                }
                recomputeCount(_items.value)
                return@launch
            }

            try {
                // logeado -> backend real
                val variants = ApiClient.catalog.listVariantsByProduct(draft.productId)
                val match = variants.firstOrNull {
                    it.sizeValue.equals(draft.talla, true) &&
                            it.color.equals(draft.color, true)
                } ?: variants.firstOrNull()

                if (match != null) {
                    ApiClient.orders.addItem(AddItemReq(match.id, draft.cantidad))
                    refreshFromBackendIfLogged()
                }
            } catch (_: Exception) {
                // no crashea
            }
        }
    }

    // ========= MODIFICAR CANTIDAD =========
    fun updateQty(item: CartItem, newQty: Int) {
        viewModelScope.launch {
            if (!isLogged() || item.itemId == null) {
                _items.update { list ->
                    list.map { if (it == item) it.copy(cantidad = newQty) else it }
                }
                recomputeCount(_items.value)
                return@launch
            }

            try {
                ApiClient.orders.updateItem(item.itemId, UpdateQtyReq(newQty))
                refreshFromBackendIfLogged()
            } catch (_: Exception) {
                // no crashea
            }
        }
    }

    // ========= ELIMINAR ITEM =========
    fun remove(item: CartItem) {
        viewModelScope.launch {
            if (!isLogged() || item.itemId == null) {
                _items.update { list -> list.filterNot { it == item } }
                recomputeCount(_items.value)
                return@launch
            }

            try {
                ApiClient.orders.deleteItem(item.itemId)
                refreshFromBackendIfLogged()
            } catch (_: Exception) {
                // no crashea
            }
        }
    }

    // ========= VACIAR =========
    fun clear() {
        viewModelScope.launch {
            if (!isLogged()) {
                _items.value = emptyList()
                recomputeCount(emptyList())
                return@launch
            }

            try {
                ApiClient.orders.clearCart()
                refreshFromBackendIfLogged()
            } catch (_: Exception) {
                // no crashea
            }
        }
    }

    private fun formatCLP(n: Int): String {
        val s = String.format(Locale.US, "%,d", n)
        return "$" + s.replace(',', '.')
    }
}
