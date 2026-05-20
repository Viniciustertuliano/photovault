package com.PhotoVault.services;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    boolean isImage(String contentType);
    byte[] generateThumbnail(MultipartFile file, int width, int height) throws IOException;

}
