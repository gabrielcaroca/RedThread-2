package com.example.redthread.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Carrito con persistencia por usuario:
 * - Invitado (sin login): funciona en memoria (como antes).
 * - Logeado: se carga/guarda en SharedPreferences bajo la clave "cart_<email>".
 */
class CartViewModel(app: Application) : AndroidViewModel(app) {

    data class CartItem(
        val productId: Int,
        val nombre: String,
        val talla: String,
        val color: String,
        val precio: String,
        val cantidad: Int = 1
    )

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    // email actual vinculado (si hay login)
    private var boundEmail: String? = null

    // ====== API pública ======

    /** Vincula el carrito al email del usuario logeado (o lo deja en invitado si es null). */
    fun bindToUserEmail(email: String?) {
        // Si no cambió, no hagas trabajo extra
        if (email == boundEmail) return

        boundEmail = email
        if (email == null) {
            // invitado: no cargamos nada de disco, seguimos en memoria
            return
        }

        // cargar carrito desde disco
        viewModelScope.launch {
            val loaded = loadFromPrefs(email)
            _items.value = loaded
            recomputeCount()
        }
    }

    /** Agrega un item al carrito. Si existe mismo (id/talla/color), acumula cantidad. */
    fun addToCart(item: CartItem) {
        _items.update { mergeItem(it, item) }
        recomputeCount()
        persistIfBound()
    }

    /** Vacía el carrito. */
    fun clear() {
        _items.value = emptyList()
        _count.value = 0
        persistIfBound()
    }

    // (Opcional) Eliminar 1 ítem exacto
    fun remove(item: CartItem) {
        _items.update { list -> list.filterNot { sameItem(it, item) } }
        recomputeCount()
        persistIfBound()
    }

    // (Opcional) Cambiar cantidad de un ítem exacto
    fun setCantidad(item: CartItem, cantidad: Int) {
        _items.update { list ->
            list.map { if (sameItem(it, item)) it.copy(cantidad = cantidad) else it }
        }
        recomputeCount()
        persistIfBound()
    }

    // ====== Internos ======

    private fun recomputeCount() {
        _count.value = _items.value.sumOf { it.cantidad }
    }

    private fun sameItem(a: CartItem, b: CartItem): Boolean =
        a.productId == b.productId && a.talla == b.talla && a.color == b.color

    private fun mergeItem(list: List<CartItem>, new: CartItem): List<CartItem> {
        val idx = list.indexOfFirst { sameItem(it, new) }
        return if (idx >= 0) {
            val cur = list[idx]
            list.toMutableList().apply {
                set(idx, cur.copy(cantidad = cur.cantidad + new.cantidad))
            }
        } else list + new
    }

    // ====== Persistencia simple (SharedPreferences + JSON) ======

    private fun persistIfBound() {
        val email = boundEmail ?: return // solo persistimos si hay login
        saveToPrefs(email, _items.value)
    }

    private fun prefs(): android.content.SharedPreferences {
        // Prefs privadas de la app
        return getApplication<Application>()
            .getSharedPreferences("redthread_cart", Context.MODE_PRIVATE)
    }

    private fun keyFor(email: String) = "cart_$email"

    private fun saveToPrefs(email: String, items: List<CartItem>) {
        val arr = JSONArray()
        items.forEach { it ->
            val o = JSONObject().apply {
                put("productId", it.productId)
                put("nombre", it.nombre)
                put("talla", it.talla)
                put("color", it.color)
                put("precio", it.precio)
                put("cantidad", it.cantidad)
            }
            arr.put(o)
        }
        prefs().edit().putString(keyFor(email), arr.toString()).apply()
    }

    private fun loadFromPrefs(email: String): List<CartItem> {
        val json = prefs().getString(keyFor(email), null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val out = ArrayList<CartItem>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    CartItem(
                        productId = o.optInt("productId"),
                        nombre = o.optString("nombre"),
                        talla = o.optString("talla"),
                        color = o.optString("color"),
                        precio = o.optString("precio"),
                        cantidad = o.optInt("cantidad", 1)
                    )
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }
}
