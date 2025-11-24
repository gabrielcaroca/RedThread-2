package com.redthread.delivery.controller;

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
public class EvidenceController {

    @Value("${app.evidence.dir:./evidence}")
    private String evidenceDir;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<InputStreamResource> getEvidence(@PathVariable String filename) throws IOException {
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
