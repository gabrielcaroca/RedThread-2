package com.example.redthread.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DeliveryApi {

    // RUTAS
    @GET("routes/active")
    suspend fun getActiveRoutes(): List<RouteDto>

    @POST("routes/{id}/take")
    suspend fun takeRoute(@Path("id") id: Long): RouteDto

    @GET("routes/{id}/shipments")
    suspend fun getShipmentsByRoute(@Path("id") id: Long): List<ShipmentDto>

    // SHIPMENTS acciones
    @POST("shipments/{id}/start")
    suspend fun startShipment(@Path("id") id: Long): ShipmentDto

    // Confirmar entrega con evidencia + gps
    @Multipart
    @POST("shipments/{id}/delivered")
    suspend fun delivered(
        @Path("id") id: Long,
        @Part("receiverName") receiverName: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part photo: MultipartBody.Part
    ): ShipmentDto

    // Fallo con evidencia + gps
    @Multipart
    @POST("shipments/{id}/fail")
    suspend fun fail(
        @Path("id") id: Long,
        @Part("note") note: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part photo: MultipartBody.Part
    ): ShipmentDto

    // Crear ruta (admin)
    @POST("routes")
    suspend fun createRoute(@Body req: CreateRouteRequest): RouteDto

}
