package com.example.redthread.data.repository

import com.example.redthread.data.local.SessionPrefs
import com.example.redthread.data.remote.AddressApi
import com.example.redthread.data.remote.dto.AddressDto
import com.example.redthread.data.remote.dto.CreateAddressRequest
import com.example.redthread.data.remote.dto.UpdateAddressRequest
import kotlinx.coroutines.flow.first

class AddressRepository(
    private val api: AddressApi,
    private val sessionPrefs: SessionPrefs
) {

    // Obtiene token desde SessionPrefs (la fuente real de sesión)
    private suspend fun getTokenHeader(): String {
        val token = sessionPrefs.tokenFlow.first() ?: ""
        if (token.isBlank()) {
            throw IllegalStateException("Error: No hay token de sesión guardado.")
        }
        return "Bearer $token"
    }

    suspend fun list(): List<AddressDto> {
        return api.list(getTokenHeader())
    }

    suspend fun create(req: CreateAddressRequest): AddressDto {
        return api.create(getTokenHeader(), req)
    }

    suspend fun update(id: Int, req: UpdateAddressRequest): AddressDto {
        return api.update(getTokenHeader(), id, req)
    }

    suspend fun delete(id: Int) {
        api.delete(getTokenHeader(), id)
    }
}
