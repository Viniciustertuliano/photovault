package com.PhotoVault.dto.response;


import com.PhotoVault.entities.UserRole;

public class PhotographerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private UserRole role;

    public PhotographerResponseDTO(Long id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public UserRole getRole() {
        return role;
    }
}
