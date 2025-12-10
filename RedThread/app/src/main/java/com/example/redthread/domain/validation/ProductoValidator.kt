package com.example.redthread.domain.validation

// Valida precio como texto desde el TextField
fun validatePrice(price: String): String? {
    if (price.isBlank()) return "El precio es obligatorio"

    val value = price.toIntOrNull()
        ?: return "Debe ser un número entero válido"

    if (value <= 0) return "El precio debe ser mayor que 0"
    // Si quieres permitir 0, cambia por: if (value < 0)

    return null
}

