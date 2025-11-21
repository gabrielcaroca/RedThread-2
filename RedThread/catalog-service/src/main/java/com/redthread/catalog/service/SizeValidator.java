package com.redthread.catalog.service;

import com.redthread.catalog.model.enums.SizeType;

import java.util.Set;

public final class SizeValidator {
    private static final Set<String> LETTERS = Set.of("XXS","XS","S","M","L","XL","XXL");

    private SizeValidator(){}

    public static void validate(SizeType type, String value) {
        if (type == null || value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tipo y valor de talla son obligatorios");
        }
        switch (type) {
            case EU -> {
                // solo números 39..46
                int n;
                try { n = Integer.parseInt(value); } 
                catch (NumberFormatException e) { throw new IllegalArgumentException("Talla EU debe ser numérica"); }
                if (n < 39 || n > 46) throw new IllegalArgumentException("Talla EU permitida 39–46");
            }
            case LETTER -> {
                String up = value.toUpperCase();
                if (!LETTERS.contains(up)) {
                    throw new IllegalArgumentException("Talla LETTER permitida: XXS, XS, S, M, L, XL, XXL");
                }
            }
        }
    }
}
