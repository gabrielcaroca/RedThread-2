package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.ImageDto;
import com.redthread.catalog.controller.dto.ImageMapper;
import com.redthread.catalog.model.ProductImage;
import com.redthread.catalog.repository.ProductImageRepository;
import com.redthread.catalog.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ImageStorageService imageStorageService;
    private final ProductImageRepository imageRepo;

    // ============================================================
    // LISTAR IMÁGENES DE UN PRODUCTO (GET)
    // ============================================================
    @GetMapping
    @Operation(
            summary = "Listar imágenes de un producto",
            description = "Devuelve todas las imágenes asociadas al producto, ordenadas por sortOrder."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de imágenes",
                    content = @Content(schema = @Schema(implementation = ImageDto.class))
            )
    })
    public List<ImageDto> list(
            @Parameter(description = "ID del producto", example = "5")
            @PathVariable Long productId
    ) {
        List<ProductImage> images = imageRepo.findByProductIdOrderBySortOrderAsc(productId);
        return images.stream()
                .map(ImageMapper::toDto)
                .toList();
    }

    // ============================================================
    // SUBIR ARCHIVO LOCAL
    // ============================================================
    @PostMapping("/upload")
    @Operation(
            summary = "Subir imagen local",
            description = "Sube un archivo multipart, lo guarda en disco y crea el registro ProductImage asociado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Imagen creada correctamente",
                    content = @Content(schema = @Schema(implementation = ImageDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Archivo inválido"),
            @ApiResponse(responseCode = "404", description = "Producto no existe")
    })
    public ResponseEntity<ImageDto> upload(
            @Parameter(description = "ID del producto", example = "5")
            @PathVariable Long productId,

            @Parameter(description = "Archivo de imagen a subir")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Marcar como imagen primaria")
            @RequestParam(name = "primary", defaultValue = "true") boolean primary
    ) throws IOException {

        ProductImage img = imageStorageService.store(productId, file, primary);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ImageMapper.toDto(img));
    }

    // ============================================================
    // DTO para pedir imagen remota
    // ============================================================
    public record ImageUrlRequest(@NotBlank String url) {}

    // ============================================================
    // SUBIR IMAGEN DESDE UNA URL REMOTA
    // ============================================================
    @PostMapping("/from-url")
    @Operation(
            summary = "Subir imagen desde URL",
            description = "Descarga una imagen remota, la guarda localmente y crea el ProductImage asociado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Imagen descargada y registrada",
                    content = @Content(schema = @Schema(implementation = ImageDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Producto no existe"),
            @ApiResponse(responseCode = "400", description = "URL inválida")
    })
    public ResponseEntity<ImageDto> fromUrl(
            @Parameter(description = "ID del producto", example = "5")
            @PathVariable Long productId,

            @Valid @RequestBody ImageUrlRequest request,

            @Parameter(description = "Marcar como imagen primaria")
            @RequestParam(name = "primary", defaultValue = "true") boolean primary
    ) throws IOException {

        URI uri = URI.create(request.url());
        String ext = getExt(request.url());
        String fileName = UUID.randomUUID() + "." + ext;

        // Carpeta temporal
        Path tempDir = Files.createTempDirectory("rt-img-" + Instant.now().toEpochMilli());
        Path tempFile = tempDir.resolve(fileName);

        // Descargar archivo remoto
        try (InputStream in = uri.toURL().openStream()) {
            Files.copy(in, tempFile);
        }

        // Guardar usando el servicio central
        ProductImage img;
        try {
            img = imageStorageService.storeFromPath(productId, tempFile, primary);
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ImageMapper.toDto(img));
    }

    // ============================================================
    // UTILIDAD: OBTENER EXTENSIÓN DE UNA URL
    // ============================================================
    private String getExt(String url) {
        int i = url.lastIndexOf('.');
        String ext = (i > 0) ? url.substring(i + 1).toLowerCase() : "jpg";

        int q = ext.indexOf('?');
        if (q >= 0) {
            ext = ext.substring(0, q);
        }

        return ext;
    }
}
