package com.PhotoVault.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PhotoVault.services.ImageService;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ImageServiceImpl implements ImageService{

    @Override
    public boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    @Override
    public byte[] generateThumbnail(MultipartFile file, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(width, height)
                .outputFormat("webp")
                .outputQuality(0.8)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
    
}
