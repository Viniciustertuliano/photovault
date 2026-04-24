package com.PhotoVault.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.PhotoVault.entities.PasswordResetToken;
import com.PhotoVault.entities.User;

import java.util.UUID;



public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>
{
    Optional<PasswordResetToken> findByToken(UUID token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate <= :now")
    void deleteExpiredTokens(Instant now);

    @Modifying
    void deleteByUser(User user);
}
