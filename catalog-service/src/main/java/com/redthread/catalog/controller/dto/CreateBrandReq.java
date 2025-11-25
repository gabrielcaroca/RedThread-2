package com.redthread.catalog.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateBrandReq", description = "Payload para crear marca.")
public record CreateBrandReq(
        @Schema(example = "Nike")
        @NotBlank String name
) {}
