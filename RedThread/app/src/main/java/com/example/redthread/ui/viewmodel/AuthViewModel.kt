package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.repository.UserRepository
import com.example.redthread.domain.validation.validateConfirm
import com.example.redthread.domain.validation.validateEmail
import com.example.redthread.domain.validation.validateNameLettersOnly
import com.example.redthread.domain.validation.validatePhoneDigitsOnly
import com.example.redthread.domain.validation.validateStrongPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class AuthHeaderState(
    val isLoggedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val role: String? = null
)

class AuthViewModel(
    private val repository: UserRepository,
    private val session: SessionPrefs
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    private val _header = MutableStateFlow(AuthHeaderState())
    val header: StateFlow<AuthHeaderState> = _header

    init {
        viewModelScope.launch {
            combine(
                session.isLoggedInFlow,
                session.userNameFlow,
                session.userEmailFlow,
                session.userRoleFlow
            ) { logged, name, email, role ->
                AuthHeaderState(isLoggedIn = logged, displayName = name, email = email, role = role)
            }.collectLatest { _header.value = it }
        }
    }

    // -------- LOGIN --------
    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(300)

            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    val user = result.getOrNull()

                    // ✅ Guarda el rol real del usuario desde SQLite
                    viewModelScope.launch {
                        session.setSession(
                            logged = true,
                            email = user?.email ?: s.email.trim(),
                            name = user?.name,
                            userId = user?.id?.toString(),
                            role = user?.role?.name ?: "USUARIO" // ✅ aquí está corregido
                        )
                    }

                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // -------- REGISTER --------
    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(400)

            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.pass
            )

            _register.update {
                if (result.isSuccess) {
                    // ✅ Todos los nuevos usuarios son USUARIO
                    viewModelScope.launch {
                        session.setSession(
                            logged = true,
                            email = s.email.trim(),
                            name = s.name.trim(),
                            userId = null,
                            role = "USUARIO"
                        )
                    }

                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }
    // === OLVIDÉ MI CONTRASEÑA ===
    // Actualiza la contraseña buscando por email o por teléfono.
    // Devuelve true si encontró un usuario y pudo actualizar; false si no existe.
    suspend fun resetPasswordByEmailOrPhone(identifier: String, newPassword: String): Boolean {
        val id = identifier.trim()
        return try {
            if (id.contains("@")) {
                // por email
                val user = repository.getUserByEmail(id) // agrega estos helpers en tu UserRepository si aún no existen
                if (user != null) {
                    repository.updatePasswordByEmail(id, newPassword)
                    true
                } else false
            } else {
                // por teléfono
                val user = repository.getUserByPhone(id)
                if (user != null) {
                    repository.updatePasswordByPhone(id, newPassword)
                    true
                } else false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // -------- LOGOUT --------
    fun logout() {
        viewModelScope.launch {
            session.clearSession()
        }
    }
}
