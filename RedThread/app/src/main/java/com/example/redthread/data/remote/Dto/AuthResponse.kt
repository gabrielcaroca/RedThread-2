package com.example.redthread.data.remote.Dto

data class AuthResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresAt: String
)
