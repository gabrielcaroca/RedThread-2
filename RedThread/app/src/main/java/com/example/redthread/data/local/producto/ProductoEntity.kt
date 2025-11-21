package com.example.redthread.data.local.producto


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val categoria: String,     //  Hombre, Mujer
    val subcategoria: String,  // Zapatilla, Pantalon, Polera, Chaqueta, Accesorio
    val precio: Long,
    val talla: String,
    val color: String,
    val imagenRes: Int,         // referencia a drawable (ej. R.drawable.ph_polera)
    val destacado: Boolean = false   // ← nuevo campo
)
