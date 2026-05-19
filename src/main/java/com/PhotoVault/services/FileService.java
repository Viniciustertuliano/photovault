package com.PhotoVault.services;

import com.PhotoVault.config.FileStorageProperties;
import com.PhotoVault.dto.request.FileUpdateDTO;
import com.PhotoVault.dto.response.FileResponseDTO;
import com.PhotoVault.entities.File;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.PermissionType;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.User;
import com.PhotoVault.exception.*;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import com.PhotoVault.repository.UserRepository;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PhotographerRepository photographerRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Path fileStorageLocation;
    private final StorageService storageService;
    private final FileValidationService fileValidationService;
    private final AccessControlService accessControlService;
    private final UserRepository userRepository;

    public FileService(FileRepository fileRepository,
                       FolderRepository folderRepository,
                       PhotographerRepository photographerRepository,
                       FileStorageProperties fileStorageProperties,
                        StorageService storageService,
                    FileValidationService fileValidationService,
                    AccessControlService accessControlService,
                    UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.storageService = storageService;
        this.fileValidationService = fileValidationService;
        this.accessControlService = accessControlService;
        this.userRepository = userRepository;


        this.fileStorageLocation = Paths.get(fileStorageProperties.getDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    private Photographer getAuthenticatedPhotographer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return photographerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Photographer", "email", email));
    }

    private FileResponseDTO toResponseDTO(File file) {
        String downloadUrl = "/api/files/" + file.getId();

        return new FileResponseDTO(
                file.getId(),
                file.getName(),
                file.getSize(),
                file.getContentType(),
                file.getUploadDate(),
                file.getFolder().getId(),
                file.getFolder().getName(),
                downloadUrl
        );
    }



    private String generateUniqueFileName(String originalFileName) {
        String extension = fileValidationService.getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    public File getFileEntity(Long fileId){
        return  fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
    }

    public Page<FileResponseDTO> getFilesByFolder(Long folderId, Pageable pageable){
        folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        return  fileRepository.findByFolderId(folderId, pageable)
                .map(this::toResponseDTO);


    }

    @Transactional
    public FileResponseDTO uploadFile (Long folderId, MultipartFile file){
        if (file.isEmpty()){
            throw new InvalidFileException("File cannot be empty");
        }

        long fileSize = file.getSize();
        Photographer photographer = getAuthenticatedPhotographer();

        int rowsUpdated = photographerRepository.allocateStorageAtoms(photographer.getId(), fileSize);
        if (rowsUpdated == 0) {
            throw new StorageQuotaExceededException("Storage limit exceeded. Please upgrade your plan.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        fileValidationService.validateFileExtension(originalFileName);
        fileValidationService.validateFileSize(file.getSize());

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        if(!folder.isOwner(photographer)){
            throw new ForbiddenException("You can only upload files to your own folders");
        }

        String storedFileName = generateUniqueFileName(originalFileName);

        try {
            String filePath = storageService.store(file, storedFileName);

            File fileEntity = new File();
            fileEntity.setName(originalFileName);
            fileEntity.setStoredName(storedFileName);
            fileEntity.setPath(filePath);
            fileEntity.setSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setUploadDate(LocalDateTime.now());
            fileEntity.setFolder(folder);

            File savedFile = fileRepository.save(fileEntity);

            return toResponseDTO(savedFile);
        }catch (IOException ex){
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }
    }

    @Transactional
    public Resource viewFile(Long fileId) {
        return getFileResourceWithPermission(fileId, PermissionType.VIEW);
    }

    @Transactional
    public Resource downloadFile(Long fileId) {
        return getFileResourceWithPermission(fileId, PermissionType.DOWNLOAD);
    }

    @Transactional
    public FileResponseDTO updateFile(Long fileId, FileUpdateDTO updateDTO){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Photographer photographer = getAuthenticatedPhotographer();

        if (!file.getFolder().isOwner(photographer)){
            throw new ForbiddenException("You do not have permission to modify this file");
        }

        if (updateDTO.name() != null && !updateDTO.name().trim().isEmpty()){
            file.setName(updateDTO.name().trim());
        }

        if (updateDTO.folderId() != null && !file.getFolder().getId().equals(updateDTO.folderId())){
            Folder targetFolder = folderRepository.findById(updateDTO.folderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", updateDTO.folderId()));

            if (!targetFolder.isOwner(photographer)){
                throw new ForbiddenException("You do not have permission to move files to this folder");
            }

            file.setFolder(targetFolder);
        }

        File updaFile = fileRepository.save(file);
        return toResponseDTO(updaFile);
    }

    @Transactional
    public void deleteFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!file.getFolder().isOwner(photographer)){
            throw new ForbiddenException("You can only delete your own files");
        }

        fileRepository.delete(file);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Resource getFileResourceWithPermission(Long fileId, PermissionType requiredPermission) {
        File fileEntity = getFileEntity(fileId);
        User currentUser = getAuthenticatedUser();

        accessControlService.validatePermission(currentUser, fileEntity.getFolder(), requiredPermission);

        try {
            return storageService.load(fileEntity.getPath());
        } catch (Exception ex) {
            throw new FileStorageException(MessageFormat.format("Could not read file: {0}", fileEntity.getName()), ex);
        }
    }
}
