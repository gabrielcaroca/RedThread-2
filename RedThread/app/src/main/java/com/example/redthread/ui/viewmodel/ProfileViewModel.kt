package com.example.redthread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.local.address.AddressEntity
import com.example.redthread.data.local.database.AppDatabase
import com.example.redthread.data.local.user.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileState(
    val user: UserEntity? = null,
    val addresses: List<AddressEntity> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val success: String? = null
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val userDao = db.userDao()
    private val addressDao = db.addressDao()
    private val prefs = SessionPrefs(app)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private var currentUserId: Int? = null

    init {
        viewModelScope.launch {
            prefs.userEmailFlow.collectLatest { email ->
                if (email.isNullOrBlank()) {
                    _state.update { it.copy(loading = false, user = null, addresses = emptyList()) }
                } else {
                    val u = userDao.getByEmail(email)
                    currentUserId = u?.id
                    _state.update { it.copy(user = u, loading = false, error = null, success = null) }

                    if (u != null) {
                        addressDao.observarPorUsuario(u.id).collectLatest { list ->
                            _state.update { s -> s.copy(addresses = list) }
                        }
                    } else {
                        _state.update { s -> s.copy(addresses = emptyList()) }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, success = null) }
    }

    /* ===== Direcciones ===== */
    fun upsertAddress(
        alias: String,
        linea1: String,
        linea2: String?,
        comuna: String,
        ciudad: String,
        region: String,
        pais: String,
        codigoPostal: String?,
        predeterminada: Boolean,
        idEdit: Long? = null
    ) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val toSave = AddressEntity(
                id = idEdit ?: 0,
                userId = uid,
                alias = alias.trim(),
                linea1 = linea1.trim(),
                linea2 = linea2?.trim(),
                comuna = comuna.trim(),
                ciudad = ciudad.trim(),
                region = region.trim(),
                pais = pais.trim(),
                codigoPostal = codigoPostal?.trim(),
                predeterminada = predeterminada
            )
            if (predeterminada) addressDao.clearDefault(uid)
            addressDao.upsert(toSave)
            _state.update { it.copy(success = "Dirección guardada") }
        }
    }

    fun setDefaultAddress(addressId: Long) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            addressDao.clearDefault(uid)
            addressDao.setDefault(addressId)
            _state.update { it.copy(success = "Predeterminada actualizada") }
        }
    }

    fun deleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            addressDao.delete(address)
            _state.update { it.copy(success = "Dirección eliminada") }
        }
    }

    /* ===== Datos de cuenta ===== */
    fun updateEmailPhone(newEmail: String, newPhone: String) {
        val u = state.value.user ?: return
        val email = newEmail.trim()
        val phone = newPhone.trim()

        // Validaciones simples
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!emailRegex.matches(email)) {
            _state.update { it.copy(error = "Correo inválido") }
            return
        }
        if (phone.length !in 7..20 || !phone.all { it.isDigit() || it == '+' || it == ' ' }) {
            _state.update { it.copy(error = "Teléfono inválido") }
            return
        }

        viewModelScope.launch {
            userDao.updateContact(u.id, email, phone)
            // Refrescar usuario en memoria
            val refreshed = userDao.getById(u.id)
            _state.update { it.copy(user = refreshed, success = "Datos actualizados", error = null) }

            // Actualiza la sesión (correo podría cambiar)
            prefs.setSession(
                logged = true,
                email = refreshed?.email,
                name = refreshed?.name,
                userId = refreshed?.id?.toString(),
                role = refreshed?.role?.name
            )
        }
    }

    fun changePassword(oldPass: String, newPass: String, confirm: String) {
        val u = state.value.user ?: return

        if (newPass.length < 6) {
            _state.update { it.copy(error = "La nueva contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (newPass != confirm) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }
        if (oldPass != u.password) {
            _state.update { it.copy(error = "La contraseña actual no es correcta") }
            return
        }

        viewModelScope.launch {
            userDao.updatePassword(u.id, newPass)
            val refreshed = userDao.getById(u.id)
            _state.update { it.copy(user = refreshed, success = "Contraseña actualizada", error = null) }
        }
    }
}
