package com.example.redthread.data.repository

import com.example.redthread.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = ApiClient.identity.login(LoginRequest(email, password))
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun register(email: String, password: String, nombre: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = ApiClient.identity.register(RegisterRequest(email, password, nombre))
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
