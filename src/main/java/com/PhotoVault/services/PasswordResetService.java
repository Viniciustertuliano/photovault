package com.PhotoVault.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PhotoVault.entities.PasswordResetToken;
import com.PhotoVault.entities.User;
import com.PhotoVault.exception.ExpiredTokenException;
import com.PhotoVault.exception.InvalidTokenException;
import com.PhotoVault.repository.PasswordResetTokenRepository;
import com.PhotoVault.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.password-reset-timeout-minutes}")
    private int timeoutMinutes;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, 
                                UserRepository userRepository, 
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public PasswordResetToken createToken(User user){

        tokenRepository.deleteByUser(user);

        UUID tokenValue = UUID.randomUUID();
        Instant expiryDate = Instant.now().plus(timeoutMinutes, ChronoUnit.MINUTES);

        PasswordResetToken resetToken = new PasswordResetToken(tokenValue, user, expiryDate);

        return tokenRepository.save(resetToken);
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
    public void resetPassword(UUID tokenValue, String newPassword){
        PasswordResetToken token = validateToken(tokenValue);

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    @Transactional
    public void forgotPassword(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()){
            User user = userOptional.get();
            PasswordResetToken token = createToken(user);

            String apiTestInstruction = "Use this token for change your password: " + token.getToken().toString();

            emailService.sendPasswordResetEmail(user.getEmail(), apiTestInstruction);
        }

    }
    
}
