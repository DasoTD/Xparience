package com.xparience.chat.message;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalChatMediaStorageService {

    private final Path rootDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "xparience-chat-media");

    public String store(MultipartFile file, String folderTag) {
        try {
            Files.createDirectories(rootDirectory);

            String extension = extractExtension(file.getOriginalFilename());
            String sanitizedFolder = sanitize(folderTag);
            String storedName = sanitizedFolder + "-" + UUID.randomUUID() + "." + extension;

            Path destination = rootDirectory.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return storedName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store media locally");
        }
    }

    public Resource load(String fileName) {
        try {
            Path candidate = rootDirectory.resolve(fileName).normalize();
            if (!candidate.startsWith(rootDirectory)) {
                throw new RuntimeException("Invalid media file path");
            }

            if (!Files.exists(candidate) || !Files.isReadable(candidate)) {
                throw new RuntimeException("Media file not found");
            }

            return new UrlResource(candidate.toUri());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read media file");
        }
    }

    public String resolveContentType(String fileName) {
        try {
            Path candidate = rootDirectory.resolve(fileName).normalize();
            String detected = Files.probeContentType(candidate);
            return detected != null ? detected : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "bin";
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return ext.isBlank() ? "bin" : ext;
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "chat";
        }

        return value.toLowerCase(Locale.ROOT)
                .replace('/', '-')
                .replace('\\', '-')
            .replaceAll("[^a-z0-9-]", "-");
    }
}
