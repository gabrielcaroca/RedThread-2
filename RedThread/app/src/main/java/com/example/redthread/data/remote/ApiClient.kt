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

    // Interceptor JWT: agrega Bearer automáticamente si hay token
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

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Microservicios
    val identity: AuthApi by lazy {
        retrofit(BaseUrls.IDENTITY).create(AuthApi::class.java)
    }

    val catalog: CatalogApi by lazy {
        retrofit(BaseUrls.CATALOG).create(CatalogApi::class.java)
    }

    val orders: OrdersApi by lazy {
        retrofit(BaseUrls.ORDERS).create(OrdersApi::class.java)
    }

    val delivery: DeliveryApi by lazy {
        retrofit(BaseUrls.DELIVERY).create(DeliveryApi::class.java)
    }

    // AddressApi vive en order-service (8083)
    val address: AddressApi by lazy {
        retrofit(BaseUrls.ORDERS).create(AddressApi::class.java)
    }
}
