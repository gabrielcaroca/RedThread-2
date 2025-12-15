package com.example.redthread.ui.viewmodel

import android.app.Application
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.repository.CatalogRepository
import com.example.redthread.utils.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo: CatalogRepository = mock()
    private val app: Application = mock()

    @Test
    fun `loadProducts success updates products and clears error`() = runTest {
        val expected = listOf(
            ProductDto(
                id = 1,
                name = "Polera",
                description = "Desc",
                basePrice = 12990.0,
                active = true,
                images = null,
                category = null,
                brand = null,
                featured = true,
                gender = "HOMBRE",
                variants = emptyList()
            )
        )
        whenever(repo.getProducts()).thenReturn(expected)

        val vm = CatalogViewModel(app, repo)

        vm.loadProducts()
        advanceUntilIdle()

        assertEquals(expected, vm.products.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `loadProducts error sets error message`() = runTest {
        whenever(repo.getProducts()).thenThrow(RuntimeException("boom"))

        val vm = CatalogViewModel(app, repo)

        vm.loadProducts()
        advanceUntilIdle()

        assertEquals("boom", vm.error.value)
    }
}
