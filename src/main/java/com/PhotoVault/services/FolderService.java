package com.PhotoVault.services;

import com.PhotoVault.dto.request.FolderOwnershipUpdateDTO;
import com.PhotoVault.dto.request.FolderRequestDTO;
import com.PhotoVault.dto.response.FolderResponseDTO;
import com.PhotoVault.dto.response.PhotographerSummaryDTO;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.User;
import com.PhotoVault.exception.ForbiddenException;
import com.PhotoVault.exception.ResourceNotFoundException;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import com.PhotoVault.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FolderService {
    private final FolderRepository folderRepository;
    private final PhotographerRepository photographerRepository;
    private final UserRepository userRepository;

    public FolderService(
            FolderRepository folderRepository,
            PhotographerRepository photographerRepository,
            UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
        this.userRepository = userRepository;
    }

    private Photographer getAuthenticatedPhotographer(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return photographerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Photographer with email " + email + " not found."));
    }

    private FolderResponseDTO toResponseDTO(Folder folder){
        List<PhotographerSummaryDTO> ownersDTO = folder.getOwners().stream()
                .map(owner -> new PhotographerSummaryDTO(owner.getId(), owner.getName()))
                .collect(Collectors.toList());
        return new FolderResponseDTO(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                ownersDTO
        );
    }

    @Transactional
    public FolderResponseDTO createFolder(FolderRequestDTO request){
        Photographer photographer = getAuthenticatedPhotographer();

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setCreatedAt(LocalDateTime.now());
        folder.addOwner(photographer);

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
        if(!folder.isOwner(photographer)){
            throw new ForbiddenException("You can only update your own folders");
        }

        folder.setName(request.getName());
        Folder updated = folderRepository.save(folder);

        return toResponseDTO(updated);
    }

    @Transactional
    public FolderResponseDTO updateOwners(Long id, FolderOwnershipUpdateDTO requestOwnersDTO){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));
        Photographer photographer = getAuthenticatedPhotographer();

        if (!folder.isOwner(photographer)){
            throw new ForbiddenException("You do not have permission to manage owners for this folder");
        }

        List<Photographer> newOwners = photographerRepository.findAllById(requestOwnersDTO.ownerIds());

        if (newOwners.isEmpty()){
            throw new IllegalArgumentException("The folder must have at least one valid owner.");
        }

        folder.setOwners(new HashSet<>(newOwners));
        Folder updatedFolder = folderRepository.save(folder);

        return toResponseDTO(updatedFolder);
    }

    @Transactional
    public void deleteFolder(Long id){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));

        Photographer photographer = getAuthenticatedPhotographer();
        if(!folder.isOwner(photographer)){
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

    public Page<FolderResponseDTO> getShareWithMe(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return folderRepository.findByFoldersSharedWithUser(user.getId(), pageable)
                .map(this::toResponseDTO);
    }
}
