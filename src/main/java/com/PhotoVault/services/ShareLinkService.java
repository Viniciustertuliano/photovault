package com.PhotoVault.services;

import com.PhotoVault.dto.request.ShareLinkRequestDTO;
import com.PhotoVault.dto.response.*;
import com.PhotoVault.entities.File;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.ShareLink;
import com.PhotoVault.exception.ForbiddenException;
import com.PhotoVault.exception.ResourceNotFoundException;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import com.PhotoVault.repository.ShareLinkRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository;
    private final FolderRepository folderRepository;
    private final PhotographerRepository photographerRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    public ShareLinkService(ShareLinkRepository shareLinkRepository,
                            FolderRepository folderRepository,
                            PhotographerRepository photographerRepository,
                            FileService fileService,
                            FileRepository fileRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    private Photographer getAuthenticatedPhotographer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return photographerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Photographer", "email", email));
    }

    private ShareLinkResponseDTO toResponseDTO(ShareLink shareLink) {
        String shareUrl = "/api/share/" + shareLink.getToken();

        return new ShareLinkResponseDTO(
                shareLink.getId(),
                shareLink.getToken(),
                shareUrl,
                shareLink.getCreatedAt(),
                shareLink.getExpirationDate(),
                shareLink.getActive(),
                shareLink.getAccessCount(),
                shareLink.getFolder().getId(),
                shareLink.getFolder().getName()
        );
    }

    public void validateTokenForFile(String token, Long fileId){
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("ShareLink", "token", token));

        if (!shareLink.isValid()){
            if(!shareLink.getActive()){
                throw new ForbiddenException("This link has been revoked");
            }
            throw new ForbiddenException("This link has expired");
        }

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        if (!file.getFolder().getId().equals(shareLink.getFolder().getId())){
            throw new ForbiddenException("This file does not belong to the shared folder");
        }
    }

    public ShareLinkResponseDTO createShareLink(Long folderId, ShareLinkRequestDTO request) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        Photographer photographer = getAuthenticatedPhotographer();

        if (!folder.getOwner().getId().equals(photographer.getId())) {
            throw new ForbiddenException("You can only create share links for your own folders");
        }

        ShareLink shareLink = new ShareLink();
        shareLink.setToken(UUID.randomUUID().toString());
        shareLink.setFolder(folder);
        shareLink.setCreatedAt(LocalDateTime.now());

        if (request.getExpirationDays() != null){
            shareLink.setExpirationDate(
                    LocalDateTime.now().plusDays(request.getExpirationDays())
            );
        }

        shareLink.setActive(true);
        shareLink.setAccessCount(0);

        ShareLink saved = shareLinkRepository.save(shareLink);

        return toResponseDTO(saved);
    }

    public FolderAccessDTO accessFolderByToken(String token) {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("ShareLink", "token", token));

        if (!shareLink.isValid()){
            if (!shareLink.getActive()){
                throw new ForbiddenException("This link has been revoked");
            }
            throw new ForbiddenException("This link has expired");
        }

        shareLink.incrementAccessCount();
        shareLinkRepository.save(shareLink);

        Folder folder = shareLink.getFolder();

        PhotographerSummaryDTO owner = new PhotographerSummaryDTO(
                folder.getOwner().getId(),
                folder.getOwner().getName()
        );

        List<FileResponseDTO> files = fileService.getFilesByFolder(folder.getId())
                .stream()
                .map(file -> new FileResponseDTO(
                        file.getId(),
                        file.getName(),
                        file.getSize(),
                        file.getContentType(),
                        file.getUploadDate(),
                        file.getFolderId(),
                        file.getFolderName(),
                        "/api/files/" + file.getId() + "?shareToken=" + token
                ))
                .collect(Collectors.toList());

        return new FolderAccessDTO(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                owner,
                files
        );
    }

    public List<ShareLinkResponseDTO> getShareLinksByFolder(Long folderId){
        folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        return shareLinkRepository.findByFolderId(folderId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void revokeShareLink(Long shareLinkId){
        ShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new ResourceNotFoundException("ShareLink", shareLinkId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!shareLink.getFolder().getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only revoke your own share links");
        }

        shareLink.setActive(false);
        shareLinkRepository.save(shareLink);
    }

    public ShareLinkResponseDTO renewShareLink(Long shareLinkId, Integer additionalDays){
        ShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new ResourceNotFoundException("ShareLink", shareLinkId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!shareLink.getFolder().getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only renew your own share links");
        }

        LocalDateTime newExpirationDate = shareLink.getExpirationDate().plusDays(additionalDays);
        shareLink.setExpirationDate(newExpirationDate);

        ShareLink updated = shareLinkRepository.save(shareLink);

        return toResponseDTO(updated);
    }
}
