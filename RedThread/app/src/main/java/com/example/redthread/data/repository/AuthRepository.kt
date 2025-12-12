package com.example.redthread.data.repository

import android.content.Context
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.AuthResponse
import com.example.redthread.data.remote.dto.LoginRequest
import com.example.redthread.data.remote.dto.RegisterRequest
import com.example.redthread.data.remote.dto.UserProfileDto
import com.example.redthread.data.remote.dto.UpdateMeRequest
import com.example.redthread.data.remote.dto.ChangePasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val session: SessionPrefs
) {

    suspend fun register(fullName: String, email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = ApiClient.identity.register(
                    RegisterRequest(fullName = fullName, email = email, password = password)
                )
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = ApiClient.identity.login(
                    LoginRequest(email = email, password = password)
                )
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================
    // PERFIL (ME)
    // =========================
    suspend fun me(tokenManual: String? = null): Result<UserProfileDto> =
        withContext(Dispatchers.IO) {
            try {
                val token = tokenManual ?: session.tokenFlow.first()

                if (token == null) {
                    return@withContext Result.failure(
                        IllegalStateException("No hay token guardado")
                    )
                }

                val resp = ApiClient.identity.me("Bearer $token")
                Result.success(resp)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================
    // UPDATE PERFIL (nombre/correo)
    // =========================
    suspend fun updateMe(fullName: String, email: String): Result<UserProfileDto> =
        withContext(Dispatchers.IO) {
            try {
                val token = session.tokenFlow.first()
                    ?: return@withContext Result.failure(IllegalStateException("No hay token guardado"))

                val resp = ApiClient.identity.updateMe(
                    token = "Bearer $token",
                    req = UpdateMeRequest(fullName = fullName, email = email)
                )

                // ✅ refrescar session para que header se actualice
                session.setSession(
                    logged = true,
                    email = resp.email,
                    name = resp.fullName,
                    userId = resp.id.toString(),
                    role = resp.roles.firstOrNull() ?: "USUARIO",
                    token = token
                )

                Result.success(resp)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================
    // CAMBIAR PASSWORD (requiere actual)
    // =========================
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val token = session.tokenFlow.first()
                    ?: return@withContext Result.failure(IllegalStateException("No hay token guardado"))

                val resp = ApiClient.identity.changePassword(
                    token = "Bearer $token",
                    req = ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )

                Result.success(resp.isSuccessful)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================
    // RESET PASSWORD
    // =========================
    suspend fun resetPassword(identifier: String, newPass: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = mapOf(
                    "identifier" to identifier,
                    "newPassword" to newPass
                )

                val resp = ApiClient.identity.resetPassword(body)
                resp.isSuccessful

            } catch (e: Exception) {
                false
            }
        }
}
