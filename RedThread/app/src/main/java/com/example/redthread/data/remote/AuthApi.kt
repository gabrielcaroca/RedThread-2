package com.example.redthread.data.remote

import com.example.redthread.data.remote.Dto.AuthResponse
import com.example.redthread.data.remote.Dto.LoginRequest
import com.example.redthread.data.remote.Dto.RegisterRequest
import com.example.redthread.data.remote.Dto.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @GET("me")
    suspend fun me(@Header("Authorization") token: String): UserProfileDto

    // 🔥 Usamos /auth/reset-password (NO /confirm)
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Void>
}
