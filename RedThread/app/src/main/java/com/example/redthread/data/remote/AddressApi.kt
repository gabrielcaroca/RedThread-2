package com.example.redthread.data.remote

import com.example.redthread.data.remote.Dto.AddressDto
import com.example.redthread.data.remote.Dto.CreateAddressRequest
import com.example.redthread.data.remote.Dto.UpdateAddressRequest
import retrofit2.http.*

interface AddressApi {

    @GET("addresses")
    suspend fun list(
        @Header("Authorization") token: String
    ): List<AddressDto>

    @POST("addresses")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body req: CreateAddressRequest
    ): AddressDto

    @PATCH("addresses/{id}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body req: UpdateAddressRequest
    ): AddressDto

    @DELETE("addresses/{id}")
    suspend fun delete(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )
}