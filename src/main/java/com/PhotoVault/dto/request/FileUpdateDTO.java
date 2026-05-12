package com.PhotoVault.dto.request;

import jakarta.validation.constraints.Size;

public record FileUpdateDTO(
    @Size(min = 5, max = 255, message = "The name must be between 5 and 255 characters long")
    String name,

    Long folderId
) {}
