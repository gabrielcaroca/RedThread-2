package com.example.redthread.data.repository

import com.example.redthread.data.remote.ApiClient
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class DespachadorRepository {

    suspend fun rutasActivas() =
        ApiClient.delivery.getRutasActivas()

    suspend fun tomarRuta(rutaId: Long) =
        ApiClient.delivery.tomarRuta(rutaId)

    suspend fun pedidosDeRuta(rutaId: Long) =
        ApiClient.delivery.getPedidosDeRuta(rutaId)

    suspend fun recogerPedido(pedidoId: Long) =
        ApiClient.delivery.recogerPedido(pedidoId)

    suspend fun confirmarEntrega(id: Long, file: File) =
        ApiClient.delivery.confirmarEntrega(
            id,
            MultipartBody.Part.createFormData(
                "foto",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
        )

    suspend fun devolverPedido(id: Long, motivo: String, file: File) =
        ApiClient.delivery.devolverPedido(
            id,
            motivo,
            MultipartBody.Part.createFormData(
                "foto",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
        )
}
