package com.example.redthread.data.remote.dto

data class AuthResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresAt: String
)
