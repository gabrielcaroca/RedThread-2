object Env {
    const val USE_USB = false   // EMULADOR usa 10.0.2.2
}



object BaseUrls {

    private const val EMULATOR_HOST = "10.0.2.2"
    private const val LOCAL_PC_IP = "192.168.100.63"  // tu red local

    // Emulador = 10.0.2.2
    // Celular USB = IP de tu PC dentro de la red (192.168.xxx)

    private val HOST =
        if (Env.USE_USB) LOCAL_PC_IP  // para celular conectado por USB
        else EMULATOR_HOST            // para emulador

    val IDENTITY = "http://$HOST:8081/"
    val CATALOG  = "http://$HOST:8082/"
    val ORDERS   = "http://$HOST:8083/"
    val DELIVERY = "http://$HOST:8084/"
}
