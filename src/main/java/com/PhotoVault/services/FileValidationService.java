package com.PhotoVault.services;

import java.util.Arrays;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.PhotoVault.config.FileStorageProperties;
import com.PhotoVault.exception.InvalidFileException;

@Service
public class FileValidationService {

    private final FileStorageProperties fileStorageProperties;
    private final Set<String> allowedExtensionsCache;

    public FileValidationService (FileStorageProperties fileStorageProperties){
        this.fileStorageProperties = fileStorageProperties;
        this.allowedExtensionsCache = Arrays.stream(fileStorageProperties.getAllowedExtensions().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public void validateFileExtension(String fileName){
        String extension = getFileExtension(fileName);

        if (!allowedExtensionsCache.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "File type not allowed. Allowed types: " +
                    fileStorageProperties.getAllowedExtensions()
            );
        }
    }

    public void validateFileSize(Long size){
        if (size > fileStorageProperties.getMaxSize()) {
            long maxSizeMB = fileStorageProperties.getMaxSize() / (1024 * 1024);
            throw new InvalidFileException(
                    "File size exceeds maximum allowed (" + maxSizeMB + "MB)"
            );
        }
    }
    
    public String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
