package com.example.redthread.data.local.ruta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutas")
data class RutaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String, // ej. "Ruta1", "Ruta2"
    val pedidosIds: String, // guardas los IDs concatenados como texto: "1,2,3"
    val activa: Boolean = true,
    val completada: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)
