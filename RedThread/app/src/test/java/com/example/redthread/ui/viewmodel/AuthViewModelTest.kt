package com.example.redthread.ui.viewmodel

import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.dto.AuthResponse
import com.example.redthread.data.remote.dto.UserProfileDto
import com.example.redthread.data.repository.AuthRepository
import com.example.redthread.utils.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepo: AuthRepository = mock()
    private val session: SessionPrefs = mock()

    private fun buildVm(): AuthViewModel {
        whenever(session.isLoggedInFlow).thenReturn(MutableStateFlow(false))
        whenever(session.userNameFlow).thenReturn(MutableStateFlow(null))
        whenever(session.userEmailFlow).thenReturn(MutableStateFlow(null))
        whenever(session.userRoleFlow).thenReturn(MutableStateFlow(null))
        return AuthViewModel(authRepo, session)
    }

    @Test
    fun `submitLogin success sets success true and calls session setSession`() = runTest {
        val vm = buildVm()

        whenever(authRepo.login(any(), any())).thenReturn(
            Result.success(
                AuthResponse(
                    tokenType = "Bearer",
                    accessToken = "token-123",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            )
        )

        whenever(authRepo.me(tokenManual = "token-123")).thenReturn(
            Result.success(
                UserProfileDto(
                    id = 99L,
                    fullName = "Pipe",
                    email = "pipe@email.com",
                    roles = listOf("CLIENTE")
                )
            )
        )

        vm.onLoginEmailChange("pipe@email.com")
        vm.onLoginPassChange("Password1!")
        vm.submitLogin()

        advanceUntilIdle()

        assertTrue(vm.login.value.success)
        assertNull(vm.login.value.errorMsg)
        assertFalse(vm.login.value.isSubmitting)

        verify(session).setSession(
            logged = true,
            email = "pipe@email.com",
            name = "Pipe",
            userId = "99",
            role = "CLIENTE",
            token = "token-123"
        )
    }

    @Test
    fun `submitLogin failure sets error message and does not call session`() = runTest {
        val vm = buildVm()

        whenever(authRepo.login(any(), any())).thenReturn(
            Result.failure(IllegalArgumentException("bad"))
        )

        vm.onLoginEmailChange("pipe@email.com")
        vm.onLoginPassChange("Password1!")
        vm.submitLogin()

        advanceUntilIdle()

        assertFalse(vm.login.value.success)
        assertEquals("Correo o contraseña incorrectos", vm.login.value.errorMsg)

        verify(session, never()).setSession(
            logged = any(),
            email = any(),
            name = any(),
            userId = any(),
            role = any(),
            token = any()
        )
    }

    @Test
    fun `clearLoginResult resets success and error`() = runTest {
        val vm = buildVm()

        vm.onLoginEmailChange("pipe@email.com")
        vm.onLoginPassChange("Password1!")
        whenever(authRepo.login(any(), any())).thenReturn(
            Result.failure(IllegalArgumentException("bad"))
        )

        vm.submitLogin()
        advanceUntilIdle()

        assertFalse(vm.login.value.success)
        assertEquals("Correo o contraseña incorrectos", vm.login.value.errorMsg)

        vm.clearLoginResult()

        assertFalse(vm.login.value.success)
        assertNull(vm.login.value.errorMsg)
    }
}
