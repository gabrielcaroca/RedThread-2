package com.example.redthread.data.remote.Dto

data class UpdateAddressRequest(
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val country: String? = null,
    val default: Boolean? = null
)
