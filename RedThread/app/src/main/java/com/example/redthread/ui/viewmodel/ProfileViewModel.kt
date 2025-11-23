// ProfileViewModel.kt
package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.Dto.AddressDto
import com.example.redthread.data.remote.Dto.CreateAddressRequest
import com.example.redthread.data.remote.Dto.UpdateAddressRequest
import com.example.redthread.data.repository.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val addresses: List<AddressDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

class ProfileViewModel(
    private val repo: AddressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private fun startLoading() {
        _state.update { it.copy(loading = true, error = null, success = null) }
    }

    private fun finishSuccess(msg: String) {
        _state.update { it.copy(loading = false, success = msg) }
    }

    private fun finishError(msg: String) {
        _state.update { it.copy(loading = false, error = msg) }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, success = null) }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            startLoading()
            try {
                val list = repo.list()
                _state.update { it.copy(addresses = list) }
                finishSuccess("Direcciones cargadas")
            } catch (e: Exception) {
                finishError(e.message ?: "Error al cargar direcciones")
            }
        }
    }

    fun createAddress(
        line1: String,
        city: String,
        state: String,
        zip: String,
        country: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            startLoading()
            try {
                val req = CreateAddressRequest(
                    line1 = line1,
                    line2 = "",          // si quieres soportar line2, agrégalo al diálogo
                    city = city,
                    state = state,
                    zip = zip,
                    country = country,
                    default = isDefault
                )
                repo.create(req)
                loadAddresses()
                finishSuccess("Dirección creada")
            } catch (e: Exception) {
                finishError(e.message ?: "Error al crear dirección")
            }
        }
    }

    fun updateAddress(
        id: Long,
        line1: String,
        city: String,
        state: String,
        zip: String,
        country: String,
        default: Boolean
    ) {
        viewModelScope.launch {
            startLoading()
            try {
                val req = UpdateAddressRequest(
                    line1 = line1,
                    line2 = "",          // igual que arriba
                    city = city,
                    state = state,
                    zip = zip,
                    country = country,
                    default = default
                )
                repo.update(id.toInt(), req)
                loadAddresses()
                finishSuccess("Dirección actualizada")
            } catch (e: Exception) {
                finishError(e.message ?: "Error al actualizar dirección")
            }
        }
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            startLoading()
            try {
                repo.delete(id.toInt())
                loadAddresses()
                finishSuccess("Dirección eliminada")
            } catch (e: Exception) {
                finishError(e.message ?: "Error al eliminar dirección")
            }
        }
    }

    fun setDefaultAddress(id: Long) {
        viewModelScope.launch {
            startLoading()
            try {
                val req = UpdateAddressRequest(default = true)
                repo.update(id.toInt(), req)
                loadAddresses()
                finishSuccess("Predeterminada actualizada")
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudo marcar como predeterminada")
            }
        }
    }
}
