package com.PhotoVault.dto.response;

public record PhotographerAuthResponseDTO(String token, String type, PhotographerResponseDTO photographerResponse) implements AuthResult {}