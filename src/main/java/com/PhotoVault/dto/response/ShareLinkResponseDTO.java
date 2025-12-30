package com.PhotoVault.dto.response;

import java.time.LocalDateTime;

public class ShareLinkResponseDTO {

    private Long id;
    private String token;
    private String shareUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expirationDate;
    private Boolean active;
    private Integer accessCount;
    private Long folderId;
    private String folderName;

    public ShareLinkResponseDTO(Long id, String token, String shareUrl, LocalDateTime createdAt, LocalDateTime expirationDate, Boolean active, Integer accessCount, Long folderId, String folderName) {
        this.id = id;
        this.token = token;
        this.shareUrl = shareUrl;
        this.createdAt = createdAt;
        this.expirationDate = expirationDate;
        this.active = active;
        this.accessCount = accessCount;
        this.folderId = folderId;
        this.folderName = folderName;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public Boolean getActive() {
        return active;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public Long getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return folderName;
    }
}
