package com.PhotoVault.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {


    String store(MultipartFile file, String fileName) throws IOException;

    String store(InputStream inputStream, String fileName, long size, String contentType) throws IOException;

    Resource load(String filePath) throws IOException;

    void delete (String filePath) throws IOException;

    boolean exists(String filePath) throws IOException;

    String getPublicUrl(String filePath);

    String generatePresignedUrl(String filePath, long expirationMinutes);
}
