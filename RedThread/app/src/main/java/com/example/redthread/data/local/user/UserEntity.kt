package com.example.redthread.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.redthread.domain.enums.UserRole
import kotlin.Boolean
import kotlin.String

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: UserRole = UserRole.USUARIO // 👈 usa el enum, no texto directo
)
