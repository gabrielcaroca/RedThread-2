package com.redthread.delivery.dto.zone;

import jakarta.validation.constraints.NotBlank;

public record CreateZoneRequest(
        @NotBlank String name,
        String city,
        String state,
        String country,
        String zipPattern
) { }
