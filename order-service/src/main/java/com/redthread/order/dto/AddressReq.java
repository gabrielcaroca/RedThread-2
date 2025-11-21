package com.redthread.order.dto;

import jakarta.validation.constraints.NotNull;

public record AddressReq(
  @NotNull String line1,
  String line2,
  @NotNull String city,
  @NotNull String state,
  @NotNull String zip,
  @NotNull String country,
  Boolean isDefault
) {}










