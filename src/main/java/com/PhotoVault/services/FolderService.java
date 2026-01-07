package com.PhotoVault.services;

import com.PhotoVault.dto.request.FolderRequestDTO;
import com.PhotoVault.dto.response.FolderResponseDTO;
import com.PhotoVault.dto.response.PhotographerSummaryDTO;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.exception.ForbiddenException;
import com.PhotoVault.exception.ResourceNotFoundException;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FolderService {
    private final FolderRepository folderRepository;
    private final PhotographerRepository photographerRepository;

    public FolderService(FolderRepository folderRepository, PhotographerRepository photographerRepository) {
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
    }

    private Photographer getAuthenticatedPhotographer(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return photographerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Photographer with email " + email + " not found."));
    }

    private FolderResponseDTO toResponseDTO(Folder folder){
        PhotographerSummaryDTO owner = new PhotographerSummaryDTO(
                folder.getOwner().getId(),
                folder.getOwner().getName()
        );
        return new FolderResponseDTO(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                owner
        );
    }

    @Transactional
    public FolderResponseDTO createFolder(FolderRequestDTO request){
        Photographer photographer = getAuthenticatedPhotographer();

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setCreatedAt(LocalDateTime.now());
        folder.setOwner(photographer);

        Folder savedFolder = folderRepository.save(folder);

        return toResponseDTO(savedFolder);
    }

    public FolderResponseDTO findById(Long id){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        return toResponseDTO(folder);
    }

    public Page<FolderResponseDTO> findAll(Pageable pageable){
        return folderRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public Page<FolderResponseDTO> findAllByOwnerId(Pageable pageable){
        Photographer photographer = getAuthenticatedPhotographer();

        return folderRepository.findByOwnerId(photographer.getId(), pageable)
                .map(this::toResponseDTO);
    }

    public Page<FolderResponseDTO> findAllByPhotographerId(Long photographerId, Pageable pageable){
        return folderRepository.findByOwnerId(photographerId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public FolderResponseDTO updateFolder(Long id, FolderRequestDTO request){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));

        Photographer photographer = getAuthenticatedPhotographer();
        if(!folder.getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only update your own folders");
        }

        folder.setName(request.getName());
        Folder updated = folderRepository.save(folder);

        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteFolder(Long id){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));

        Photographer photographer = getAuthenticatedPhotographer();
        if(!folder.getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only delete your own folders");
        }

        folderRepository.delete(folder);
    }

    @Transactional
    public FolderResponseDTO restoreFolder(Long id){
        Folder folder = folderRepository.findByIdIncluidingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));

        folder.restore();
        Folder restored = folderRepository.save(folder);

        return toResponseDTO(restored);
    }
}
