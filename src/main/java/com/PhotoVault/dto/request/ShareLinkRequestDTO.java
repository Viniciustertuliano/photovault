package com.PhotoVault.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

public class ShareLinkRequestDTO {

    @Min(value = 1, message = "Expiration days must be at least 1")
    private Integer expirationDays = 7;

    @Email(message = "Invalid email format")
    private String clientEmail;

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

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    
}
