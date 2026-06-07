package com.ridemate.util;

import com.ridemate.exception.AppException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Component
public class FileStorageUtil {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    private static final Set<String> ALLOWED_IMAGE = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_DOC   = Set.of("jpg", "jpeg", "png", "pdf");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
    }

    /**
     * Stores a photo (JPG/PNG only). Returns the stored file path.
     * @param subDir e.g. "profiles", "parcels", "documents"
     */
    public String storeImage(MultipartFile file, String subDir) {
        return store(file, subDir, ALLOWED_IMAGE);
    }

    /**
     * Stores a document (JPG/PNG/PDF). Returns the stored file path.
     * @param subDir e.g. "aadhaar", "dl"
     */
    public String storeDocument(MultipartFile file, String subDir) {
        return store(file, subDir, ALLOWED_DOC);
    }

    private String store(MultipartFile file, String subDir, Set<String> allowed) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File must not be empty.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxSizeBytes) {
            throw new AppException("File exceeds the 5 MB size limit.", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String original = file.getOriginalFilename();
        String ext = getExtension(original);
        if (!allowed.contains(ext.toLowerCase())) {
            throw new AppException(
                    "Unsupported file type '" + ext + "'. Allowed: " + allowed, HttpStatus.BAD_REQUEST);
        }

        try {
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "." + ext;
            Path dest = dir.resolve(filename);
            file.transferTo(dest.toFile());
            // Return a relative path suitable for serving via FileController
            return subDir + "/" + filename;
        } catch (IOException e) {
            throw new AppException("Failed to store file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Deletes a file by its relative path (won't throw if it doesn't exist). */
    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Files.deleteIfExists(Paths.get(uploadDir, relativePath));
        } catch (IOException ignored) { }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new AppException("File has no extension.", HttpStatus.BAD_REQUEST);
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
