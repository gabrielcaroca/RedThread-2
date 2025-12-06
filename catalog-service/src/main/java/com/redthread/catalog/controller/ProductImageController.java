package com.redthread.catalog.controller;

import com.redthread.catalog.service.ImageStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ImageStorageService imageStorageService;

    // =========================
    // Subir archivo local
    // =========================
    @PostMapping("/upload")
    public ResponseEntity<Void> upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "primary", defaultValue = "true") boolean primary
    ) throws IOException {

        // Guarda la imagen y crea ProductImage + relación con Product
        imageStorageService.store(productId, file, primary);

        // No devolvemos el ProductImage para evitar problemas de lazy loading
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DTO para la URL remota
    public record ImageUrlRequest(@NotBlank String url) {}

    // =========================
    // Subir imagen desde URL remota
    // =========================
    @PostMapping("/from-url")
    public ResponseEntity<Void> fromUrl(
            @PathVariable Long productId,
            @RequestBody @Valid ImageUrlRequest request,
            @RequestParam(name = "primary", defaultValue = "true") boolean primary
    ) throws IOException {

        URI uri = URI.create(request.url());
        String ext = getExt(request.url());
        String fileName = UUID.randomUUID() + "." + ext;

        // Carpeta temporal para descargar la imagen
        Path tempDir = Files.createTempDirectory("rt-img-" + Instant.now().toEpochMilli());
        Path tempFile = tempDir.resolve(fileName);

        try (InputStream in = uri.toURL().openStream()) {
            Files.copy(in, tempFile);
        }

        try {
            // Reutilizamos tu lógica central en el servicio
            imageStorageService.storeFromPath(productId, tempFile, primary);
        } finally {
            // Limpiamos archivos temporales
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =========================
    // Auxiliar para sacar extensión
    // =========================
    private String getExt(String url) {
        int i = url.lastIndexOf('.');
        String ext = (i > 0) ? url.substring(i + 1).toLowerCase() : "jpg";
        // Limpiar query params tipo ?width=...
        int q = ext.indexOf('?');
        if (q >= 0) {
            ext = ext.substring(0, q);
        }
        return ext;
    }
}
