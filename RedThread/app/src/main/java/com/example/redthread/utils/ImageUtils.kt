package com.example.redthread.utils

import com.example.redthread.data.remote.BaseUrls

fun absImage(url: String?): String? {
    if (url.isNullOrBlank()) return null
    
    if (url.startsWith("http", ignoreCase = true)) {
        return url
    }
    val base = BaseUrls.CATALOG.trimEnd('/')

    return base + url
}
