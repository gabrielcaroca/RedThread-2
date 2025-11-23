package com.example.redthread.data.repository

import android.content.Context
import com.example.redthread.data.AuthStorage
import com.example.redthread.data.remote.AddressApi
import com.example.redthread.data.remote.Dto.*

class AddressRepository(
    private val api: AddressApi,
    private val context: Context // 👈 Agregamos esto para acceder al AuthStorage
) {

    // Función auxiliar para obtener el token o lanzar error si no hay sesión
    private suspend fun getTokenHeader(): String {
        val token = AuthStorage.getToken(context)
            ?: throw IllegalStateException("Error: No hay token de sesión guardado.")
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