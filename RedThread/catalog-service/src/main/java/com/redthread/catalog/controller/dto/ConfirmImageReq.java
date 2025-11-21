package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.*;

public record ConfirmImageReq(
        @NotBlank String key
) {}
