package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.OrdersApi
import com.example.redthread.data.remote.dto.AdminOrderDetailRes
import com.example.redthread.data.remote.dto.OrderDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminPedidoDetalleViewModel(
    private val api: OrdersApi
) : ViewModel() {

    private val _order = MutableStateFlow<AdminOrderDetailRes?>(null)
    val order: StateFlow<AdminOrderDetailRes?> = _order

    fun loadOrder(id: Long) {
        viewModelScope.launch {
            _order.value = api.getAdminOrderDetail(id)
        }
    }
}

