package com.example.redthread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.R
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.local.producto.ProductoDao
import com.example.redthread.data.local.producto.ProductoEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Categoria { Featured, Hombre, Mujer }
enum class Subcategoria { Zapatilla, Pantalon, Polera, Chaqueta, Accesorio }

class ProductoViewModel(app: Application) : AndroidViewModel(app) {

    private val dao: ProductoDao = AppDatabase.getInstance(app).productoDao()

    private val _categoria = MutableStateFlow(Categoria.Featured)
    val categoria: StateFlow<Categoria> = _categoria

    // ✅ Todos los productos (para DeveloperScreen)
    val productos: StateFlow<List<ProductoEntity>> =
        dao.observarTodos()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ✅ Solo los destacados (para HomeScreen)
    val destacados: StateFlow<List<ProductoEntity>> =
        dao.obtenerDestacados()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { precargarDatos() }
    }

    fun setCategoria(c: Categoria) {
        _categoria.value = c
    }

    private suspend fun precargarDatos() {
        if (dao.count() > 0) return

        val iniciales = listOf(
            ProductoEntity(
                nombre = "Polera Basic",
                descripcion = "Polera de algodón cómoda",
                categoria = "Hombre",
                subcategoria = "Polera",
                precio = 12990,
                talla = "M",
                color = "Negro",
                imagenRes = R.drawable.ph_polera,
                destacado = true
            ),
            ProductoEntity(
                nombre = "Chaqueta Urban",
                descripcion = "Chaqueta ligera con estilo moderno",
                categoria = "Mujer",
                subcategoria = "Chaqueta",
                precio = 29990,
                talla = "L",
                color = "Gris",
                imagenRes = R.drawable.ph_chaqueta,
                destacado = false
            ),
            ProductoEntity(
                nombre = "Pantalón Slim",
                descripcion = "Pantalón de mezclilla entallado",
                categoria = "Hombre",
                subcategoria = "Pantalon",
                precio = 24990,
                talla = "42",
                color = "Azul",
                imagenRes = R.drawable.ph_pantalon,
                destacado = false
            ),
            ProductoEntity(
                nombre = "Zapatillas Urban",
                descripcion = "Zapatillas deportivas estilo urbano",
                categoria = "Mujer",
                subcategoria = "Zapatilla",
                precio = 59990,
                talla = "38",
                color = "Rojo",
                imagenRes = R.drawable.ph_zapatillas,
                destacado = true
            ),
            ProductoEntity(
                nombre = "Accesorio Arena",
                descripcion = "Gorro o accesorio moderno",
                categoria = "Hombre",
                subcategoria = "Accesorio",
                precio = 7990,
                talla = "-",
                color = "Arena",
                imagenRes = R.drawable.ph_accesorio,
                destacado = false
            )
        )

        iniciales.forEach { dao.upsert(it) }
    }
}
