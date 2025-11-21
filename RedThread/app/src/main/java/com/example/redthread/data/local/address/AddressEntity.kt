package com.example.redthread.data.local.address

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "address",
    indices = [Index("userId")]
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int,
    val alias: String,                 // "Casa", "Trabajo", etc.
    val linea1: String,               // Calle y número
    val linea2: String? = null,       // Dpto, block… (opcional)
    val comuna: String,
    val ciudad: String,
    val region: String,
    val pais: String = "Chile",
    val codigoPostal: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val predeterminada: Boolean = false
)
