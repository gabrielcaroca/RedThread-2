package com.example.redthread.data.repository

import android.content.Context
import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.dto.AuthResponse
import com.example.redthread.data.remote.dto.LoginRequest
import com.example.redthread.data.remote.dto.RegisterRequest
import com.example.redthread.data.remote.dto.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val session: SessionPrefs
) {

    // =========================
    // LOGIN
    // =========================
    suspend fun login(email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val req = LoginRequest(email, password)
                val resp = ApiClient.identity.login(req)

                // Guardamos token en segundo plano

                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================
    // REGISTER
    // =========================
    suspend fun register(fullName: String, email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                println("🔥 Enviando registro → $fullName / $email / $password")
                val req = RegisterRequest(fullName, email, password)
                val resp = ApiClient.identity.register(req)
                println("🔥 Respuesta del registro: $resp")

                // Guardamos token en segundo plano
                //AuthStorage.saveToken(context, resp.accessToken) aSDIBASUDASUJDAIDBNISDIUBASIDBASUIBDUI
                Result.success(resp)
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Error en registro: ${e.message}")
                Result.failure(e)
            }
        }

    // =========================
    // PERFIL (ME) - ¡ACTUALIZADO!
    // =========================
    // Ahora acepta un tokenManual opcional.
    // Si el ViewModel se lo pasa (justo después de loguear), usa ese.
    // Si no se lo pasa (cuando abres la app días después), lee del disco.
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