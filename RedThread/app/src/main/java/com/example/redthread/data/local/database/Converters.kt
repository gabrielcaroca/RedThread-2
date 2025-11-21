package com.example.redthread.data.local.database

import androidx.room.TypeConverter
import com.example.redthread.domain.enums.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }
}
