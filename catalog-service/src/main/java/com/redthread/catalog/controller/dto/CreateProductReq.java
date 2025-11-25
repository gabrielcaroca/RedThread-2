package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.enums.ProductGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(name = "CreateProductReq", description = "Payload para crear/actualizar un producto base.")
public record CreateProductReq(
        @Schema(example = "1", description = "ID de la categoría")
        @NotNull Long categoryId,

        @Schema(example = "2", description = "ID de la marca (opcional)")
        Long brandId,

        @Schema(example = "Polera Oversize Negra", description = "Nombre del producto")
        @NotBlank String name,

        @Schema(example = "Polera de algodón 100% con corte oversize.", description = "Descripción (opcional)")
        String description,

        @Schema(example = "12990.00", description = "Precio base del producto")
        @NotNull @DecimalMin(value="0.0", inclusive = true) BigDecimal basePrice,

        @Schema(example = "false", description = "Producto destacado para home")
        @NotNull Boolean featured,

        @Schema(example = "HOMBRE", description = "Género del producto: HOMBRE o MUJER")
        @NotNull ProductGender gender
) {}
