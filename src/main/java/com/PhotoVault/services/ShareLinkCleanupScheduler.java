package com.PhotoVault.services;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.PhotoVault.repository.ShareLinkRepository;

import jakarta.transaction.Transactional;

@Component
public class ShareLinkCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShareLinkCleanupScheduler.class);
    private final ShareLinkRepository shareLinkRepository;

    public ShareLinkCleanupScheduler(ShareLinkRepository shareLinkRepository) {
        this.shareLinkRepository = shareLinkRepository;
    }

    @Transactional
    @Scheduled(cron = "${app.scheduling.cleanup-links-cron}")
    public void cleanupExpiredLinks() {
        logger.info("Starting a routine to clean up expired or inactive ShareLinks...");
        
        try {
            int deletedCount = shareLinkRepository.deleteExpiredLinks(LocalDateTime.now());

            logger.info("ShareLinks cleanup completed successfully. {} records removed.", deletedCount);
            
        } catch (Exception e) {
            logger.info("Fatal error while trying to clear expired ShareLinks: {}", e.getMessage());
        }
    }


}
