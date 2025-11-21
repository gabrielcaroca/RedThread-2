package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandReq(@NotBlank String name) { }


