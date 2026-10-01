package com.careconnect.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadBaseDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "pdf", "svg"
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo no puede estar vacío"));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de archivo no permitido. Formatos válidos: JPG, PNG, WEBP, GIF, PDF"));
        }

        // Sanitizar el nombre de carpeta
        String safeFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");
        if (safeFolder.isBlank()) {
            safeFolder = "general";
        }

        try {
            Path targetDir = Paths.get(uploadBaseDir, safeFolder).toAbsolutePath().normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String uniqueName = UUID.randomUUID().toString() + "." + extension.toLowerCase();
            Path targetFile = targetDir.resolve(uniqueName);

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/api/v1/uploads/" + safeFolder + "/" + uniqueName;

            return ResponseEntity.ok(Map.of(
                    "url", publicUrl,
                    "fileName", uniqueName,
                    "originalName", originalFilename,
                    "size", file.getSize(),
                    "contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar el archivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{folder}/{fileName:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String fileName) {

        String safeFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");
        String safeFileName = Paths.get(fileName).getFileName().toString();

        try {
            Path filePath = Paths.get(uploadBaseDir, safeFolder, safeFileName).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String extension = StringUtils.getFilenameExtension(safeFileName);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            if (extension != null) {
                switch (extension.toLowerCase()) {
                    case "jpg", "jpeg" -> mediaType = MediaType.IMAGE_JPEG;
                    case "png" -> mediaType = MediaType.IMAGE_PNG;
                    case "webp" -> mediaType = MediaType.parseMediaType("image/webp");
                    case "gif" -> mediaType = MediaType.IMAGE_GIF;
                    case "pdf" -> mediaType = MediaType.APPLICATION_PDF;
                    case "svg" -> mediaType = MediaType.parseMediaType("image/svg+xml");
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}