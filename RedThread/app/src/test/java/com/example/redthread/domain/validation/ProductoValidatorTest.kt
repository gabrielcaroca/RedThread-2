package com.example.redthread.domain.validation

import org.junit.Assert.*
import org.junit.Test

class ProductoValidatorTest {

    @Test
    fun `validatePrice empty returns error`() {
        val result = validatePrice("")
        assertEquals("El precio es obligatorio", result)
    }

    @Test
    fun `validatePrice non numeric returns error`() {
        val result = validatePrice("abc")
        assertEquals("Debe ser un número entero válido", result)
    }

    @Test
    fun `validatePrice zero returns error`() {
        val result = validatePrice("0")
        assertEquals("El precio debe ser mayor que 0", result)
    }

    @Test
    fun `validatePrice valid returns null`() {
        val result = validatePrice("12990")
        assertNull(result)
    }

    @Test
    fun `validateStock empty returns error`() {
        val result = validateStock("")
        assertEquals("El stock es obligatorio", result)
    }

    @Test
    fun `validateStock non numeric returns error`() {
        val result = validateStock("x")
        assertEquals("Debe ser un número entero válido", result)
    }

    @Test
    fun `validateStock zero returns error`() {
        val result = validateStock("0")
        assertEquals("El stock debe ser mayor que 0", result)
    }

    @Test
    fun `validateStock valid returns null`() {
        val result = validateStock("5")
        assertNull(result)
    }
}
