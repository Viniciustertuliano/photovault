package com.PhotoVault.services;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PhotoVault.entities.File;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;

@Service
public class StorageCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StorageCleanupScheduler.class);

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final PhotographerRepository photographerRepository;


    public StorageCleanupScheduler(FileRepository fileRepository, FolderRepository folderRepository,
            StorageService storageService, PhotographerRepository photographerRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.storageService = storageService;
        this.photographerRepository = photographerRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredStorage() {
        logger.info("Starting Garbage Collection from Storage (Soft Delete Cleanup)...");

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        processExpiredFiles(threshold);
        processExpiredFolders(threshold);

        logger.info("Garbage Collection completed successfully.");
    }

    private void processExpiredFiles(LocalDateTime threshold) {
        List<File> expiredFiles = fileRepository.findExpiredFiles(threshold);

        if (expiredFiles.isEmpty()) {
            return;
        }

        logger.info("Expired files found {} for physical deletion.", expiredFiles.size());

        for (File file : expiredFiles) {
            try {
                if (file.getThumbnailPath() != null) {
                    storageService.delete(file.getThumbnailPath());
                }
                
                storageService.delete(file.getPath());

                fileRepository.hardDeleteById(file.getId());

                Long primaryOwnerId = file.getFolder().getOwners().stream()
                        .findFirst()
                        .map(photographer -> photographer.getId())
                        .orElse(null);
                photographerRepository.freeUpStorage(primaryOwnerId, file.getSize());


                logger.debug("Physical file deleted and {} bytes returned to the Photographer {}", file.getSize(), primaryOwnerId);
            } catch (Exception e) {
                logger.error("Failed to delete physical file. ID: {}, Path: {}. Error: {}", file.getId(), file.getPath(), e.getMessage());
            }
        }
    }

    private void processExpiredFolders(LocalDateTime threshold) {
        List<Folder> expiredFolders = folderRepository.findExpiredFolders(threshold);

        for (Folder folder : expiredFolders) {
            try {
                folderRepository.hardDeleteById(folder.getId());
                logger.debug("Expired folder removed from the DB: {}", folder.getId());
            } catch (Exception e) {
                logger.error("Failed to remove folder from database. ID: {}. Error: {}", folder.getId(), e.getMessage());
            }
        }
    }
    

}
