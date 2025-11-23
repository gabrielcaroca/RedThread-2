package com.example.redthread.data.remote

import com.example.redthread.data.local.SessionPrefs
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.runBlocking

object ApiClient {

    //   SESSION (Token desde DataStore)
    private lateinit var session: SessionPrefs

    fun init(sessionPrefs: SessionPrefs) {
        session = sessionPrefs
    }

    //   INTERCEPTOR JWT
    private val authInterceptor = Interceptor { chain ->

        val token = runBlocking {
            session.tokenFlow.firstOrNull() ?: ""
        }

        val newReq = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()

        chain.proceed(newReq)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    //   RETROFIT GENERADOR
    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    //   MICROSERVICIOS

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

    //  NUEVO: AddressApi (usa el microservicio identity-service puerto 8081)
    val address: AddressApi by lazy {
        retrofit(BaseUrls.IDENTITY).create(AddressApi::class.java)
    }
}
