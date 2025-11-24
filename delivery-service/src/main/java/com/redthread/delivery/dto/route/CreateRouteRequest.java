package com.redthread.delivery.dto.route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateRouteRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotEmpty List<Long> orderIds,
        Long totalPrice
) {}
