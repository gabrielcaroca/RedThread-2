package com.redthread.delivery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/evidence")
@Tag(name="Evidence", description="Acceso a imágenes de evidencia (entregado/fallido)")
public class EvidenceController {

    @Value("${app.evidence.dir:./evidence}")
    private String evidenceDir;

    @GetMapping("/{filename:.+}")
    @Operation(summary="Descargar evidencia", description="Sirve una imagen guardada en disco.")
    public ResponseEntity<InputStreamResource> getEvidence(
            @Parameter(description="Nombre exacto del archivo", example="delivered_10_1732579200000.jpg")
            @PathVariable String filename
    ) throws IOException {

        if (!StringUtils.hasText(filename) || filename.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        Path file = Paths.get(evidenceDir).toAbsolutePath().normalize().resolve(filename);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        var resource = new InputStreamResource(Files.newInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
