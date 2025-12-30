package com.PhotoVault.dto.request;


import jakarta.validation.constraints.Min;

public class ShareLinkRequestDTO {

    @Min(value = 1, message = "Expiration days must be at least 1")
    private Integer expirationDays = 7;


    public ShareLinkRequestDTO() {
    }

    public ShareLinkRequestDTO(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }
}
