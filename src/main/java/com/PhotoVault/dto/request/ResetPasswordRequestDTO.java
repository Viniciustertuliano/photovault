package com.PhotoVault.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
    @NotNull(message = "Token is required")
    UUID token,

    @NotBlank(message = "The new cannot be blank")
    @Size(min = 8, max = 64, message = "The password must be between 8 and 64 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "The password must contain at least one uppercase letter, one lowercase letter, one number, and one special character." )
    String newPassword
){}