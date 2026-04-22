package com.PhotoVault.dto.response;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public class FolderAccessDTO {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final PhotographerSummaryDTO owner;
    private final Page<FileResponseDTO> files;

    public FolderAccessDTO(Long id, String name, LocalDateTime createdAt, PhotographerSummaryDTO owner, Page<FileResponseDTO> files) {
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

    public Page<FileResponseDTO> getFiles() {
        return files;
    }
}
