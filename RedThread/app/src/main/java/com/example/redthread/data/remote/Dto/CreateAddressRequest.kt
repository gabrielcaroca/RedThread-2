package com.example.redthread.data.remote.Dto

data class CreateAddressRequest(
    val line1: String,
    val line2: String? = null,
    val city: String,
    val state: String,
    val zip: String,
    val country: String,
    val default: Boolean
)
