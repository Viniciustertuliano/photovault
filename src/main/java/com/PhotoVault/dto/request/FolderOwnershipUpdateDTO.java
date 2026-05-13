package com.PhotoVault.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record FolderOwnershipUpdateDTO(
    @NotEmpty(message = "The folder must have at least one owner.")
    List<Long> ownerIds
) {}