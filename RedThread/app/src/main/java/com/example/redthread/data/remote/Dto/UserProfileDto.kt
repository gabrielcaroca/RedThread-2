package com.example.redthread.data.remote.Dto

data class UserProfileDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val roles: List<String>
)

data class RoleDto(
    val key: String
)
