package com.example.redthread.data.remote

import com.example.redthread.ui.viewmodel.Pedido
import com.example.redthread.ui.viewmodel.Ruta

fun RutaDto.toRutaUi() = Ruta(
    id = id,
    nombre = nombre,
    descripcion = descripcion ?: "",
    totalPedidos = totalPedidos
)

fun PedidoDto.toPedidoUi() = Pedido(
    id = id.toInt(),
    nombre = nombre,
    imagen = imagen,
    estado = estado,
    direccion = direccion,
    mensaje = mensaje ?: "",
    motivoDevolucion = motivoDevolucion ?: ""
)
