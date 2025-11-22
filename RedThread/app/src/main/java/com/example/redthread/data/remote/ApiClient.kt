package com.example.redthread.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private fun build(baseUrl: String): Retrofit {
        println("➡️ Base URL usada por Retrofit: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }



    // Cliente IdentityService
    val identity: AuthApi = build(BaseUrls.IDENTITY).create(AuthApi::class.java)
}
