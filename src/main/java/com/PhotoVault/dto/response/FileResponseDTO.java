package com.PhotoVault.dto.response;

import java.time.LocalDateTime;

public class FileResponseDTO {
    private Long id;
    private String name;
    private Long size;
    private String ContentType;
    private LocalDateTime uploadDate;
    private Long folderId;
    private String FolderName;
    private String downloadUrl;

    public FileResponseDTO(Long id, String name, Long size, String contentType, LocalDateTime uploadDate, Long folderId, String folderName, String downloadUrl) {
        this.id = id;
        this.name = name;
        this.size = size;
        ContentType = contentType;
        this.uploadDate = uploadDate;
        this.folderId = folderId;
        FolderName = folderName;
        this.downloadUrl = downloadUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public String getContentType() {
        return ContentType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public Long getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return FolderName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
