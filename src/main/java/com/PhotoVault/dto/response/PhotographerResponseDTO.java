package com.PhotoVault.dto.response;


import com.PhotoVault.entities.UserRole;

public record PhotographerResponseDTO(
        Long id,
        String name,
        String email,
        UserRole role,
        Long storageQuotaBytes,
        Long storageUsedBytes) {
        
    public double getStorageUsagePercentage() {
        if (storageQuotaBytes == null || storageQuotaBytes == 0) return 0.0;
        return ((double) storageUsedBytes / storageQuotaBytes) * 100;
    }
}
