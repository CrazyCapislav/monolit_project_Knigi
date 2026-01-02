package dev.petr.file.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path storageLocation;
    private final long maxFileSize;

    public FileStorageService(
            @Value("${file.storage.location}") String storageLocation,
            @Value("${file.storage.max-size}") long maxFileSize) {
        this.storageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;

        try {
            Files.createDirectories(this.storageLocation);
            log.info("Storage location initialized: {}", this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            Path destinationFile = this.storageLocation.resolve(storedFilename)
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.storageLocation.toAbsolutePath())) {
                throw new IllegalArgumentException("Cannot store file outside storage directory");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully: {}", storedFilename);

            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Resource load(String filename) {
        try {
            Path file = storageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("File not found: " + filename, e);
        }
    }

    public void delete(String filename) {
        try {
            Path file = storageLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public String getFilePath(String filename) {
        return storageLocation.resolve(filename).toString();
    }
}