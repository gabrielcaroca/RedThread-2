package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.redthread.data.remote.OrdersApi

class AdminPedidoDetalleVmFactory(
    private val api: OrdersApi
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminPedidoDetalleViewModel(api) as T
    }
}
