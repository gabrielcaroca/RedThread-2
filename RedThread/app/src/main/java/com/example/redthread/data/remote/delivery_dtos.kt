package com.example.redthread.data.remote

data class RutaDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = "",
    val totalPedidos: Int = 0,
    val activa: Boolean = true,
    val tomada: Boolean = false
)

data class PedidoDto(
    val id: Long,
    val nombre: String,
    val imagen: String,
    val estado: String,
    val direccion: String,
    val mensaje: String? = "",
    val motivoDevolucion: String? = ""
)
