package com.PhotoVault.services;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetLink);

    void sendShareLinkEmail(String toEmail, String photographerName, String folderName, String token);
}
