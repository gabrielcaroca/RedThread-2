package com.redthread.catalog.controller;

import com.redthread.catalog.model.ProductImage;
import com.redthread.catalog.service.ImageStorageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    // ---- Subir imagen local ----
    @PostMapping("/upload")
    public ProductImage uploadLocal(
            @PathVariable Long productId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) throws IOException {
        return imageStorageService.store(productId, file, false);
    }

    // ---- NUEVO: Subir imagen desde URL ----
    @PostMapping("/from-url")
    public ProductImage uploadFromUrl(
            @PathVariable Long productId,
            @RequestBody ImageUrlRequest req
    ) throws IOException {
        if (req.url() == null || req.url().isBlank())
            throw new IllegalArgumentException("URL vacía o inválida");

        // Descargar imagen temporalmente
        URI imageUri = URI.create(req.url());
        String ext = getExt(req.url());
        Path tempFile = Files.createTempFile("img_" + UUID.randomUUID(), "." + ext);

        try (InputStream in = imageUri.toURL().openStream()) {
            Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Guardar usando el servicio
        ProductImage saved = imageStorageService.storeFromPath(productId, tempFile, false);

        // Borrar temporal
        Files.deleteIfExists(tempFile);

        return saved;
    }

    private String getExt(String url) {
        int i = url.lastIndexOf('.');
        String ext = (i > 0) ? url.substring(i + 1).toLowerCase() : "jpg";
        if (ext.contains("?")) ext = ext.substring(0, ext.indexOf('?')); // limpia ?width etc.
        return ext;
    }

    public record ImageUrlRequest(@NotBlank String url) {}
}
