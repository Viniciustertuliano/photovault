package com.PhotoVault.services;

import com.PhotoVault.config.FileStorageProperties;
import com.PhotoVault.dto.response.FileResponseDTO;
import com.PhotoVault.entities.File;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.exception.*;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
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

    public FileService(FileRepository fileRepository,
                       FolderRepository folderRepository,
                       PhotographerRepository photographerRepository,
                       FileStorageProperties fileStorageProperties,
                        StorageService storageService,
                    FileValidationService fileValidationService) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
        this.fileStorageProperties = fileStorageProperties;
        this.storageService = storageService;
        this.fileValidationService = fileValidationService;

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

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        fileValidationService.validateFileExtension(originalFileName);
        fileValidationService.validateFileSize(file.getSize());

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!folder.getOwner().getId().equals(photographer.getId())){
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

    public Resource downloadFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        try {
            return storageService.load(file.getPath());
        }catch (Exception ex) {
            throw new FileStorageException("File not found: " + file.getName(), ex);
        }
    }

    @Transactional
    public void deleteFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!file.getFolder().getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only delete your own files");
        }

        try {
            storageService.delete(file.getPath());
        }catch (IOException ex){
            throw new FileStorageException("Could not delete file: " + file.getName(), ex);
        }

        fileRepository.delete(file);
    }


    public void validateAccessForFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", id));

        Photographer photographer = getAuthenticatedPhotographer();

        if (!file.getFolder().getOwner().getId().equals(photographer.getId())) {
            throw new ForbiddenException("You do not have access to this file");
        }
    }
}
