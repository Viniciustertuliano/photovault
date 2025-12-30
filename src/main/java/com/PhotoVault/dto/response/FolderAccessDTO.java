package com.PhotoVault.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class FolderAccessDTO {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final PhotographerSummaryDTO owner;
    private final List<FileResponseDTO> files;

    public FolderAccessDTO(Long id, String name, LocalDateTime createdAt, PhotographerSummaryDTO owner, List<FileResponseDTO> files) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.owner = owner;
        this.files = files;
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

    public List<FileResponseDTO> getFiles() {
        return files;
    }
}
