package com.example.redthread.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.repository.AuthRepository
import com.example.redthread.domain.validation.validateConfirm
import com.example.redthread.domain.validation.validateEmail
import com.example.redthread.domain.validation.validateNameLettersOnly
import com.example.redthread.domain.validation.validateStrongPassword
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
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
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
    private val authRepo: AuthRepository,
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
                AuthHeaderState(
                    isLoggedIn = logged,
                    displayName = name,
                    email = email,
                    role = role
                )
            }.collectLatest { _header.value = it }
        }
    }

    // -------------------- LOGIN --------------------
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

            val result = authRepo.login(s.email.trim(), s.pass.trim())

            if (result.isSuccess) {
                val loginResponse = result.getOrNull()
                val rawToken = loginResponse?.accessToken

                // Token Manual para evitar Race Condition
                val me = authRepo.me(tokenManual = rawToken)

                if (me.isSuccess) {
                    val profile = me.getOrNull()

                    session.setSession(
                        logged = true,
                        email = profile?.email,
                        name = profile?.fullName,
                        userId = profile?.id?.toString(),
                        role = profile?.roles?.firstOrNull() ?: "CLIENTE",
                        token = rawToken      // ⭐ IMPORTANTE
                    )

                } else {
                    _login.update {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = "No se pudo obtener el perfil"
                        )
                    }
                    return@launch
                }

                _login.update { it.copy(isSubmitting = false, success = true) }

            } else {
                _login.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = "Correo o contraseña incorrectos"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // -------------------- REGISTER --------------------
    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
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

    // (compatibilidad) lo que ya usabas antes
    suspend fun resetPasswordByEmailOrPhone(identifier: String, newPass: String): Boolean {
        return authRepo.confirmResetPassword(identifier, newPass)
    }

    suspend fun requestResetCode(identifier: String): Boolean {
        return authRepo.requestResetCode(identifier)
    }

    suspend fun confirmResetPassword(identifier: String, newPass: String): Boolean {
        return authRepo.confirmResetPassword(identifier, newPass)
    }


    fun clearRegisterResult() {
        _register.update {
            it.copy(
                success = false,
                isSubmitting = false,
                errorMsg = null
            )
        }
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() &&
                s.email.isNotBlank() &&
                s.pass.isNotBlank() &&
                s.confirm.isNotBlank()

        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            val result = authRepo.register(
                fullName = s.name.trim(),
                email = s.email.trim(),
                password = s.pass.trim()
            )

            if (result.isSuccess) {
                val regResponse = result.getOrNull()
                val rawToken = regResponse?.accessToken

                val me = authRepo.me(tokenManual = rawToken)

                if (me.isSuccess) {
                    val profile = me.getOrNull()
                    session.setSession(
                        logged = true,
                        email = profile?.email ?: s.email.trim(),
                        name = profile?.fullName ?: s.name.trim(),
                        userId = profile?.id?.toString(),
                        role = profile?.roles?.firstOrNull() ?: "CLIENTE",
                        token = rawToken
                    )

                }

                _register.update { it.copy(isSubmitting = false, success = true) }

            } else {
                _register.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { session.clearSession() }
    }
}
