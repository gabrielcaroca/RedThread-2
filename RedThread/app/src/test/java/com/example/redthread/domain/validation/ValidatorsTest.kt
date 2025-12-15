package com.example.redthread.domain.validation

import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `validateEmail blank returns error`() {
        assertEquals(
            "El email es obligatorio",
            validateEmail("")
        )
    }

    @Test
    fun `validateEmail invalid returns error`() {
        assertEquals(
            "Formato de email inválido",
            validateEmail("correo-malo")
        )
    }

    @Test
    fun `validateEmail valid returns null`() {
        assertNull(validateEmail("test@email.com"))
    }

    @Test
    fun `validateNameLettersOnly invalid returns error`() {
        assertEquals(
            "Solo letras y espacios",
            validateNameLettersOnly("Juan123")
        )
    }

    @Test
    fun `validateNameLettersOnly valid returns null`() {
        assertNull(validateNameLettersOnly("Juan Pérez"))
    }

    @Test
    fun `validateStrongPassword weak returns error`() {
        assertEquals(
            "Mínimo 8 caracteres",
            validateStrongPassword("Aa1!")
        )
    }

    @Test
    fun `validateStrongPassword without uppercase returns error`() {
        assertEquals(
            "Debe incluir una mayúscula",
            validateStrongPassword("abcde1!@")
        )
    }

    @Test
    fun `validateStrongPassword valid returns null`() {
        assertNull(validateStrongPassword("Password1!"))
    }

    @Test
    fun `validateConfirm mismatch returns error`() {
        assertEquals(
            "Las contraseñas no coinciden",
            validateConfirm("1234", "4321")
        )
    }

    @Test
    fun `validateConfirm match returns null`() {
        assertNull(validateConfirm("Password1!", "Password1!"))
    }
}
