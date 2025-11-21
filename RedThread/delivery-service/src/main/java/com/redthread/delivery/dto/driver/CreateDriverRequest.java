package com.redthread.delivery.dto.driver;

import jakarta.validation.constraints.NotBlank;

public record CreateDriverRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String email,
        Boolean active
) { }
