package com.example.redthread.data.repository

import com.example.redthread.data.remote.CatalogApi
import com.example.redthread.data.remote.dto.BrandDto
import com.example.redthread.data.remote.dto.CategoryDto
import com.example.redthread.data.remote.dto.ProductDto
import com.example.redthread.data.remote.dto.VariantDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CatalogRepositoryTest {

    private val api: CatalogApi = mock()
    private val repo = CatalogRepository(api)

    @Test
    fun `getCategories delegates to api`() = runTest {
        val expected = listOf(
            CategoryDto(
                id = 1,
                name = "Poleras",
                description = null,
                active = true
            )
        )
        whenever(api.listCategories()).thenReturn(expected)

        val result = repo.getCategories()

        assertEquals(expected, result)
        verify(api).listCategories()
    }

    @Test
    fun `getBrands delegates to api`() = runTest {
        val expected = listOf(
            BrandDto(
                id = 2,
                name = "Nike",
                active = true
            )
        )
        whenever(api.listBrands()).thenReturn(expected)

        val result = repo.getBrands()

        assertEquals(expected, result)
        verify(api).listBrands()
    }

    @Test
    fun `getProducts delegates to api`() = runTest {
        val expected = listOf(
            ProductDto(
                id = 5,
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
        whenever(api.listProducts()).thenReturn(expected)

        val result = repo.getProducts()

        assertEquals(expected, result)
        verify(api).listProducts()
    }

    @Test
    fun `getVariant delegates to api`() = runTest {
        val expected = VariantDto(
            id = 100L,
            productId = 5L,
            sizeType = "LETTER",
            sizeValue = "M",
            color = "NEGRO",
            sku = "SKU-100",
            priceOverride = null,
            stock = null
        )
        whenever(api.getVariant(100L)).thenReturn(expected)

        val result = repo.getVariant(100L)

        assertEquals(expected, result)
        verify(api).getVariant(100L)
    }
}
