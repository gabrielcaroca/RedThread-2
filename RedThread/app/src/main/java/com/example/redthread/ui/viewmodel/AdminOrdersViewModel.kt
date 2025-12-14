package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.OrderRes
import com.example.redthread.data.remote.OrdersApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminOrdersViewModel(
    private val api: OrdersApi
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderRes>>(emptyList())
    val orders: StateFlow<List<OrderRes>> = _orders

    fun loadOrders() {
        viewModelScope.launch {
            _orders.value = api.listOrders()
        }
    }
}