package com.example.redthread.data.remote

import com.example.redthread.data.remote.dto.AuthResponse
import com.example.redthread.data.remote.dto.LoginRequest
import com.example.redthread.data.remote.dto.RegisterRequest
import com.example.redthread.data.remote.dto.UserProfileDto
import com.example.redthread.data.remote.dto.UpdateMeRequest
import com.example.redthread.data.remote.dto.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @GET("me")
    suspend fun me(@Header("Authorization") token: String): UserProfileDto

    @PATCH("me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body req: UpdateMeRequest
    ): UserProfileDto

    @POST("me/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body req: ChangePasswordRequest
    ): Response<Void>

    // Paso 1: validar que exista el usuario (y "enviar" código)
    @POST("auth/reset-password")
    suspend fun requestResetPassword(@Body body: Map<String, String>): Response<Void>

    // Paso 2: confirmar y cambiar la contraseña realmente
    @POST("auth/reset-password/confirm")
    suspend fun confirmResetPassword(@Body body: Map<String, String>): Response<Void>
}
