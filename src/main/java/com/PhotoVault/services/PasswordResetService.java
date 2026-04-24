package com.PhotoVault.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.PhotoVault.entities.PasswordResetToken;
import com.PhotoVault.exception.ExpiredTokenException;
import com.PhotoVault.exception.InvalidTokenException;
import com.PhotoVault.repository.PasswordResetTokenRepository;

import io.swagger.v3.oas.annotations.servers.Server;

@Server
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.security.password-reset-timeout-minutes}")
    private int timeoutMinutes;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public PasswordResetToken validateToken(UUID tokenValue) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Token does not exist or is invalid."));
        
        if (token.isUsed()){
            throw new InvalidTokenException("This token has already been used.");
        }

        if (token.isExpired()){
            throw new ExpiredTokenException("The recovery link has expired.");
        }

        return token;
    }

    @Transactional
    public void MaskAsUsed(PasswordResetToken token){
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }
    
}
