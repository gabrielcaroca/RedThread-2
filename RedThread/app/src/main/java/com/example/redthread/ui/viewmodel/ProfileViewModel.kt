package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.data.remote.dto.CreateAddressRequest
import com.example.redthread.data.remote.dto.UpdateAddressRequest
import com.example.redthread.data.repository.AddressRepository
import com.example.redthread.data.repository.AuthRepository
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
    private val repo: AddressRepository,
    private val authRepo: AuthRepository
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
                _state.update { it.copy(addresses = list, loading = false) }
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudieron cargar direcciones")
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
                    line2 = "",
                    city = city,
                    state = state,
                    zip = zip,
                    country = country,
                    default = false
                )
                repo.create(req)
                loadAddresses()
                finishSuccess("Dirección creada")
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudo crear dirección")
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
                    line2 = "",
                    city = city,
                    state = state,
                    zip = zip,
                    country = country,
                    default = false
                )
                repo.update(id.toInt(), req)
                loadAddresses()
                finishSuccess("Dirección actualizada")
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudo actualizar dirección")
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
                finishError(e.message ?: "No se pudo eliminar dirección")
            }
        }
    }

    fun updateProfile(fullName: String, email: String) {
        viewModelScope.launch {
            startLoading()
            try {
                val res = authRepo.updateMe(fullName, email).getOrThrow()
                finishSuccess("Perfil actualizado")
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudo actualizar el perfil")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            startLoading()
            try {
                val ok = authRepo.changePassword(currentPassword, newPassword).getOrThrow()
                if (ok) finishSuccess("Contraseña actualizada")
                else finishError("No se pudo actualizar la contraseña")
            } catch (e: Exception) {
                finishError(e.message ?: "No se pudo actualizar la contraseña")
            }
        }
    }
}
