package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.*;

public record PresignImageReq(
        @NotBlank String filename
) {}

