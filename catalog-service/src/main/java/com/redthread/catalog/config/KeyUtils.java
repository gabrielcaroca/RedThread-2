package com.redthread.catalog.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class KeyUtils {
    private KeyUtils() {}

    public static SecretKey hmacKey(String secret) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        // Nombre del algoritmo debe coincidir con HS256 → HmacSHA256
        return new SecretKeySpec(key, "HmacSHA256");
    }
}
