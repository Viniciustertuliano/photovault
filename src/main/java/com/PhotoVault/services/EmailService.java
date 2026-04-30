package com.PhotoVault.services;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetLink);
}
