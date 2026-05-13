package com.PhotoVault.repository;

import com.PhotoVault.entities.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByToken(String token);

    List<ShareLink> findByFolderId(Long folderId);

    @Modifying
    @Query("DELETE FROM ShareLink s WHERE s.expirationDate < :now OR s.active = false")
    int deleteExpiredLinks(@Param("now") LocalDateTime now);
}
