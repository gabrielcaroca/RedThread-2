package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddressRes", description = "Respuesta de dirección")
public record AddressRes(
    @Schema(description = "ID de dirección", example = "3")
    Long id,
    @Schema(description = "Linea 1", example = "Av. Siempre Viva 123")
    String line1,
    @Schema(description = "Linea 2", example = "Depto 45")
    String line2,
    @Schema(description = "Ciudad", example = "Santiago")
    String city,
    @Schema(description = "Región/Estado", example = "RM")
    String state,
    @Schema(description = "Código postal", example = "8320000")
    String zip,
    @Schema(description = "País", example = "Chile")
    String country,
    @Schema(description = "Es default", example = "true")
    boolean isDefault
) {
  // constructor legacy usado en deliveryDetail
  public AddressRes(String line1, String line2, String city, String state, String zip, String country) {
    this(null, line1, line2, city, state, zip, country, false);
  }
}
