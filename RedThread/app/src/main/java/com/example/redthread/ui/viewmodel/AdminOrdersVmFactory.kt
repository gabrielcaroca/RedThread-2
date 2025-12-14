package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.redthread.data.remote.OrdersApi

class AdminOrdersVmFactory(
    private val api: OrdersApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminOrdersViewModel::class.java)) {
            return AdminOrdersViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
