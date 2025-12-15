package com.example.redthread.ui.viewmodel

import android.app.Application
import com.example.redthread.utils.MainDispatcherRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val app: Application = mock()

    @Test
    fun `can create viewmodel`() {
        val vm = CartViewModel(app)
        assertNotNull(vm)
    }
}
