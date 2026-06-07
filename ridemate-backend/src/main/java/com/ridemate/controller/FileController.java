package com.ridemate.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * GET /api/files/{subDir}/{filename}
     * Serves uploaded files. Requires authentication (enforced by SecurityConfig).
     */
    @GetMapping("/{subDir}/{filename}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String subDir,
            @PathVariable String filename) {

        // Basic path traversal prevention
        if (filename.contains("..") || subDir.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(subDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".png"))  return "image/png";
        return "image/jpeg";
    }
}
