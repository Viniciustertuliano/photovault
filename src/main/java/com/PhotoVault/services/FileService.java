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
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PhotographerRepository photographerRepository;
    private final FileStorageProperties fileStorageProperties;
    private final Path fileStorageLocation;

    public FileService(FileRepository fileRepository,
                       FolderRepository folderRepository,
                       PhotographerRepository photographerRepository,
                       FileStorageProperties fileStorageProperties) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.photographerRepository = photographerRepository;
        this.fileStorageProperties = fileStorageProperties;

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

    private void validadeFileExtension(String fileName){
        String extension = getFileExtension(fileName);

        List<String> allowedExtensions = Arrays.asList(
                fileStorageProperties.getAllowedExtensions().split(",")
        );
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "File type not allowed. Allowed types: " +
                    fileStorageProperties.getAllowedExtensions()
            );
        }
    }

    private void validateFileSize(Long size){
        if (size > fileStorageProperties.getMaxSize()) {
            long maxSizeMB = fileStorageProperties.getMaxSize() / (1024 * 1024);
            throw new InvalidFileException(
                    "File size exceeds maximum allowed (" + maxSizeMB + "MB)"
            );
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    public File getFileEntity(Long fileId){
        return  fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
    }

    public List<FileResponseDTO> getFilesByFolder(Long folderId){
        folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        List<File> files = fileRepository.findByFolderId(folderId);

        return files.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public FileResponseDTO uploadFile (Long folderId, MultipartFile file){
        if (file.isEmpty()){
            throw new InvalidFileException("File cannot be empty");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        validadeFileExtension(originalFileName);
        validateFileSize(file.getSize());

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!folder.getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only upload files to your own folders");
        }

        String storedFileName = generateUniqueFileName(originalFileName);
        Path targetLocation = this.fileStorageLocation.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException ex){
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }

        File fileEntity = new File();
        fileEntity.setName(originalFileName);
        fileEntity.setStoredName(storedFileName);
        fileEntity.setPath(targetLocation.toString());
        fileEntity.setSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setUploadDate(LocalDateTime.now());
        fileEntity.setFolder(folder);

        File savedFile = fileRepository.save(fileEntity);

        return toResponseDTO(savedFile);
    }

    public Resource downloadFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        try {
            Path filePath = Paths.get(file.getPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()){
                return resource;
            }else {
                throw new ResourceNotFoundException("File", "path", file.getPath());
            }
        }catch (Exception ex) {
            throw new FileStorageException("File not found: " + file.getName(), ex);
        }
    }

    public void deleteFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Photographer photographer = getAuthenticatedPhotographer();

        if(!file.getFolder().getOwner().getId().equals(photographer.getId())){
            throw new ForbiddenException("You can only delete your own files");
        }

        try {
            Path filePath = Paths.get(file.getPath()).normalize();
            Files.deleteIfExists(filePath);
        }catch (IOException ex){
            throw new FileStorageException("Could not delete file: " + file.getName(), ex);
        }

        fileRepository.delete(file);
    }


    public void validateAccessForFile(String shareToken, Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", id));

        Photographer photographer = getAuthenticatedPhotographer();

        if (!file.getFolder().getOwner().getId().equals(photographer.getId())) {
            throw new ForbiddenException("You do not have access to this file");
        }
    }
}
