package com.example.redthread.data.remote.dto

data class AddressDto(
    val id: Long,
    val line1: String,
    val line2: String?,
    val city: String,
    val state: String,
    val zip: String,
    val country: String,
    val default: Boolean
)
