package com.redthread.delivery.integration.dto;

public record ShippingAddress(
        String line1,
        String line2,
        String city,
        String state,
        String zip,
        String country
) {}
