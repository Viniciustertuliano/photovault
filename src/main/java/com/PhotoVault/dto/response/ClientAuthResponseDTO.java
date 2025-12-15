package com.PhotoVault.dto.response;

public record ClientAuthResponseDTO(String token, String type, ClientResponseDTO client) implements AuthResult {}
