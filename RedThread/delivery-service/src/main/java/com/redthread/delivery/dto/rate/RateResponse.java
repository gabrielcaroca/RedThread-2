package com.redthread.delivery.dto.rate;

public record RateResponse(Long id, Long zoneId, String basePrice, String pricePerKm, boolean isActive) { }
