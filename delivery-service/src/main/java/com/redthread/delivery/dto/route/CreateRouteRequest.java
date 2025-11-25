package com.redthread.delivery.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(name = "CreateRouteRequest", description = "Crea una ruta con una lista de orders.")
public record CreateRouteRequest(

        @Schema(example = "Ruta Centro", description = "Nombre visible de la ruta")
        @NotBlank String nombre,

        @Schema(example = "Pedidos del centro de Santiago", description = "Descripción opcional")
        String descripcion,

        @Schema(example = "[101,102,103]", description = "IDs de órdenes que componen la ruta")
        @NotEmpty List<Long> orderIds,

        @Schema(example = "45990", description = "Total estimado de la ruta (opcional, backend puede recalcular)")
        Long totalPrice
) {}
