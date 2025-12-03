package com.PhotoVault.dto.response;

import com.PhotoVault.entities.UserRole;
import jakarta.validation.constraints.Size;

public class ClientResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private UserRole role;

    public ClientResponseDTO(Long id, String name, String email, String phoneNumber, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }
}
