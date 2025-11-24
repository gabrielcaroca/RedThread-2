package com.redthread.delivery.domain;

public enum DeliveryStatus {
    PENDING_PICKUP,   // creado, esperando retiro en bodega
    ASSIGNED,         // asignado a despachador (ruta tomada o asignación directa)
    IN_TRANSIT,       // en ruta hacia cliente
    DELIVERED,        // entregado con evidencia
    FAILED,           // no se pudo entregar (retorno)
    RETURNED,         // retornado a bodega
    CANCELLED         // cancelado antes de completarse
}
