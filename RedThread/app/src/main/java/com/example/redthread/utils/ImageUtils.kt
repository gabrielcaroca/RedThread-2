// app/src/main/java/com/example/redthread/utils/ImageUtils.kt
package com.example.redthread.utils

import com.example.redthread.data.remote.BaseUrls

/**
 * Convierte un path relativo de imagen (ej: "/media/products/1/xxx.png")
 * en una URL absoluta usando la misma base que el microservicio de catálogo.
 *
 * - Si la URL ya empieza con "http", se devuelve tal cual.
 * - Si es null o vacía, se devuelve null.
 * - Si es relativa, se combina con BaseUrls.CATALOG.
 */
fun absImage(url: String?): String? {
    if (url.isNullOrBlank()) return null

    // Si ya es absoluta, no tocamos nada
    if (url.startsWith("http", ignoreCase = true)) {
        return url
    }

    // Base del catálogo, asegurando que no termine con "/" duplicado
    val base = BaseUrls.CATALOG.trimEnd('/')

    // En el backend el publicUrl viene como "/media/..." → lo concatenamos
    return base + url
}
