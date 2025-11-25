package com.redthread.catalog.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name="CreateCategoryReq", description = "Payload para crear/actualizar categorías.")
public record CreateCategoryReq(
        @Schema(example = "Poleras", description = "Nombre único de la categoría")
        @NotBlank String name,

        @Schema(example = "Ropa superior casual", description = "Descripción opcional")
        String description
) {}
