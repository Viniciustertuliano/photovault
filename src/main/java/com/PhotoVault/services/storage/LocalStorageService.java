package com.PhotoVault.services.storage;

import com.PhotoVault.exception.FileStorageException;
import com.PhotoVault.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path storageLocation;

    public LocalStorageService(@Value("${file.upload.dir}") String uploadDir) {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.storageLocation);
            logger.info("Local storage initialized at: {}", this.storageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String fileName) throws IOException {
        return store(file.getInputStream(), fileName, file.getSize(), file.getContentType());
    }

    @Override
    public String store(InputStream inputStream, String fileName, long size, String contentType) throws IOException {
        try {
            Path targetLocation = this.storageLocation.resolve(fileName);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File '{}' stored locally", fileName);
            return targetLocation.toString();

        } catch (IOException e) {
            throw new FileStorageException("Could not store file: " + fileName, e);
        }
    }

    @Override
    public Resource load(String filePath) throws IOException {
        try {
            Path file = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (Exception e) {
            throw new FileStorageException("Could not load file: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) throws IOException {
        try {
            Path file = Paths.get(filePath).normalize();
            Files.deleteIfExists(file);
            logger.info("File '{}' deleted", filePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + filePath, e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    public String getPublicUrl(String filePath) {
        // Não há URL pública em storage local
        return null;
    }

    @Override
    public String generatePresignedUrl(String filePath, long expirationMinutes) {
        // Não suportado em storage local
        return null;
    }
}