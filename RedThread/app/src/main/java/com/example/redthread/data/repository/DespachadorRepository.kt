package com.example.redthread.data.repository

import com.example.redthread.data.remote.ApiClient
import com.example.redthread.data.remote.DeliveryApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DespachadorRepository(
    private val api: DeliveryApi = ApiClient.delivery
) {
    suspend fun rutasActivas() = api.getActiveRoutes()
    suspend fun tomarRuta(id: Long) = api.takeRoute(id)
    suspend fun shipmentsDeRuta(routeId: Long) = api.getShipmentsByRoute(routeId)

    suspend fun startShipment(id: Long) = api.startShipment(id)

    suspend fun delivered(
        id: Long,
        receiverName: String,
        evidencia: File,
        lat: Double?,
        lng: Double?
    ) = api.delivered(
        id = id,
        receiverName = receiverName.toRequestBody("text/plain".toMediaTypeOrNull()),
        latitude = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        longitude = lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        photo = evidencia.toMultipart("photo")
    )

    suspend fun fail(
        id: Long,
        note: String,
        evidencia: File,
        lat: Double?,
        lng: Double?
    ) = api.fail(
        id = id,
        note = note.toRequestBody("text/plain".toMediaTypeOrNull()),
        latitude = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        longitude = lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        photo = evidencia.toMultipart("photo")
    )

    private fun File.toMultipart(partName: String): MultipartBody.Part {
        val reqBody: RequestBody = this.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, this.name, reqBody)
    }
}
