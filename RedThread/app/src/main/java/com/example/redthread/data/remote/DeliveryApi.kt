package com.example.redthread.data.remote

import okhttp3.MultipartBody
import retrofit2.http.*

interface DeliveryApi {

    @GET("api/rutas/activas")
    suspend fun getRutasActivas(): List<RutaDto>

    @POST("api/rutas/{rutaId}/tomar")
    suspend fun tomarRuta(@Path("rutaId") rutaId: Long): RutaDto

    @GET("api/rutas/{rutaId}/pedidos")
    suspend fun getPedidosDeRuta(@Path("rutaId") rutaId: Long): List<PedidoDto>

    @POST("api/pedidos/{pedidoId}/recoger")
    suspend fun recogerPedido(@Path("pedidoId") pedidoId: Long): PedidoDto

    @Multipart
    @POST("api/pedidos/{pedidoId}/entregar")
    suspend fun confirmarEntrega(
        @Path("pedidoId") pedidoId: Long,
        @Part foto: MultipartBody.Part
    ): PedidoDto

    @Multipart
    @POST("api/pedidos/{pedidoId}/devolver")
    suspend fun devolverPedido(
        @Path("pedidoId") pedidoId: Long,
        @Part("motivo") motivo: String,
        @Part foto: MultipartBody.Part
    ): PedidoDto
}
