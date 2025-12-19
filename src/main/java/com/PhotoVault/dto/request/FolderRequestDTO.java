package com.PhotoVault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FolderRequestDTO {

    @NotBlank(message = "Folder name is required")
    @Size(min = 5, max = 100, message = "Folder name must be between 5 and 100 characters")
    private String name;

    public FolderRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
