package com.example.redthread.ui.viewmodel

import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.data.remote.dto.CreateAddressRequest
import com.example.redthread.data.repository.AddressRepository
import com.example.redthread.data.repository.AuthRepository
import com.example.redthread.utils.MainDispatcherRule
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
import org.mockito.kotlin.whenever

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo: AddressRepository = mock()
    private val authRepo: AuthRepository = mock()

    @Test
    fun `loadAddresses success fills addresses`() = runTest {
        val expected = listOf(
            AddressDto(
                id = 1L,
                line1 = "Calle 1",
                line2 = null,
                city = "Santiago",
                state = "RM",
                zip = "0000000",
                country = "CL",
                default = false
            )
        )
        whenever(repo.list()).thenReturn(expected)

        val vm = ProfileViewModel(repo, authRepo)

        vm.loadAddresses()
        advanceUntilIdle()

        assertFalse(vm.state.value.loading)
        assertEquals(expected, vm.state.value.addresses)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `createAddress success adds address to list`() = runTest {
        whenever(repo.list()).thenReturn(emptyList())
        whenever(repo.create(any<CreateAddressRequest>())).thenReturn(
            AddressDto(
                id = 2L,
                line1 = "Calle 2",
                line2 = null,
                city = "Santiago",
                state = "RM",
                zip = "0000000",
                country = "CL",
                default = false
            )
        )

        val vm = ProfileViewModel(repo, authRepo)

        vm.loadAddresses()
        advanceUntilIdle()

        vm.createAddress(
            line1 = "Calle 2",
            city = "Santiago",
            state = "RM",
            zip = "0000000",
            country = "CL",
            isDefault = false
        )
        advanceUntilIdle()

        assertTrue(vm.state.value.addresses.any { it.id == 2L })
    }

    @Test
    fun `loadAddresses error sets error and stops loading`() = runTest {
        whenever(repo.list()).thenThrow(RuntimeException("fail"))

        val vm = ProfileViewModel(repo, authRepo)

        vm.loadAddresses()
        advanceUntilIdle()

        assertFalse(vm.state.value.loading)
        assertEquals("fail", vm.state.value.error)
    }
}
