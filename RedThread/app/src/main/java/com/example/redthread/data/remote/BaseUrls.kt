package com.example.redthread.data.remote

object Env {
    const val USE_USB = true
}

object BaseUrls {

    private const val LOCAL_PC_IP = "192.168.100.63"

    private val HOST = if (Env.USE_USB) "127.0.0.1" else LOCAL_PC_IP

    val IDENTITY = "http://$HOST:8081/"
    val CATALOG  = "http://$HOST:8082/"
    val ORDERS   = "http://$HOST:8083/"
    val DELIVERY = "http://$HOST:8084/"
}
