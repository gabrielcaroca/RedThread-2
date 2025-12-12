package com.example.redthread.data.remote.dto

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
