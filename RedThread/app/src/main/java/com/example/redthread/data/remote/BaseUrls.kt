package com.example.redthread.data.remote

object Env {
    // false = emulador (10.0.2.2)
    // true  = telefono USB sin wifi (127.0.0.1 usando adb reverse)
    const val USE_USB = true
}

object BaseUrls {

    private const val EMULATOR_HOST = "10.0.2.2"
    private const val USB_HOST = "127.0.0.1" // para adb reverse sin wifi
    private const val LOCAL_PC_IP = "192.168.100.63" // para wifi

    /*
      MODOS:
      - Emulador: Env.USE_USB = false  -> HOST = 10.0.2.2
      - Telefono USB sin wifi: Env.USE_USB = true -> HOST = 127.0.0.1 (adb reverse)
      - Telefono por wifi: cambia USB_HOST por LOCAL_PC_IP o crea un flag aparte
    */

    private val HOST =
        if (Env.USE_USB) USB_HOST
        else EMULATOR_HOST

    val IDENTITY = "http://$HOST:8081/"
    val CATALOG  = "http://$HOST:8082/"
    val ORDERS   = "http://$HOST:8083/"
    val DELIVERY = "http://$HOST:8084/"
}
