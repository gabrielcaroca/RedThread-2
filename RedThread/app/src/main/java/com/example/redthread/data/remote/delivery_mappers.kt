package com.example.redthread.data.remote

import com.example.redthread.ui.viewmodel.Pedido
import com.example.redthread.ui.viewmodel.Ruta

// ====== Mapper real para tu despachador ======
fun ShipmentDto.toPedido(): Pedido {
    val addr = listOfNotNull(
        addressLine1,
        addressLine2,
        city,
        state,
        zip,
        country
    ).joinToString(", ")

    return Pedido(
        id = id.toInt(),            // shipmentId
        orderId = orderId,
        nombre = "Pedido #$orderId",
        imagen = "ic_box",
        estado = status ?: "PENDING_PICKUP",
        direccion = if (addr.isBlank()) "Sin dirección" else addr
    )
}

fun RouteDto.toRuta(): Ruta {
    return Ruta(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        totalPedidos = totalPedidos,
        totalPrice = totalPrice
    )
}
