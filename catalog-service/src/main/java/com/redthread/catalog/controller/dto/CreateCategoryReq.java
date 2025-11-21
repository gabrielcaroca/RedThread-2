package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.*;

public record CreateCategoryReq(
        @NotBlank String name,
        String description
) {}
