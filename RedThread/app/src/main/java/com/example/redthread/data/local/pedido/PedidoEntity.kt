package com.example.redthread.data.local.pedido

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedido")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val direccion: String,
    val total: Long,
    val productos: String, // formato JSON o texto simple
    val fecha: Long = System.currentTimeMillis(),
    val entregado: Boolean = false, // lo marca el repartidor
    val estado: String = "pendiente"
)
