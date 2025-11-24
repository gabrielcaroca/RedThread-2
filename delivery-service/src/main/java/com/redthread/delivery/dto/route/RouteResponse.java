package com.redthread.delivery.dto.route;

public record RouteResponse(
        Long id,
        String nombre,
        String descripcion,
        Integer totalPedidos,
        Long totalPrice,
        Boolean activa,
        Long assignedUserId
) {}
