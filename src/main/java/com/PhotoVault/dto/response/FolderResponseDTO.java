package com.PhotoVault.dto.response;

import java.time.LocalDateTime;

public class FolderResponseDTO {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private PhotographerSummaryDTO owner;

    public FolderResponseDTO(Long id, String name, LocalDateTime createdAt, PhotographerSummaryDTO owner) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PhotographerSummaryDTO getOwner() {
        return owner;
    }
}
