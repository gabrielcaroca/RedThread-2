package com.example.redthread.data.remote

import com.example.redthread.data.local.SessionPrefs
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Session (token desde DataStore)
    private lateinit var session: SessionPrefs

    fun init(sessionPrefs: SessionPrefs) {
        session = sessionPrefs
    }

    // ==========================
    // Interceptor JWT
    // ==========================
    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking {
            if (::session.isInitialized) session.tokenFlow.firstOrNull() ?: "" else ""
        }

        val req = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(req)
    }

    // Cliente CON auth (para endpoints protegidos)
    private val authedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    // Cliente SIN auth (para catálogo público)
    private val plainClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ==========================
    // Microservicios
    // ==========================

    // Auth / identity -> usa token
    val identity: AuthApi by lazy {
        retrofit(BaseUrls.IDENTITY, authedClient).create(AuthApi::class.java)
    }

    // Catalog -> SIN token (público: productos, variantes, imágenes)
    val catalog: CatalogApi by lazy {
        retrofit(BaseUrls.CATALOG, plainClient).create(CatalogApi::class.java)
    }

    // Orders -> requiere token
    val orders: OrdersApi by lazy {
        retrofit(BaseUrls.ORDERS, authedClient).create(OrdersApi::class.java)
    }

    // Delivery -> requiere token
    val delivery: DeliveryApi by lazy {
        retrofit(BaseUrls.DELIVERY, authedClient).create(DeliveryApi::class.java)
    }

    val address: AddressApi by lazy {
        retrofit(BaseUrls.ORDERS, authedClient).create(AddressApi::class.java)
    }
}
