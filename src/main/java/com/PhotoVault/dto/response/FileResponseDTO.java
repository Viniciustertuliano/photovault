package com.PhotoVault.dto.response;

import com.PhotoVault.controller.FileController;
import com.PhotoVault.controller.FolderController;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponseDTO extends RepresentationModel<FileResponseDTO> {
    private final Long id;
    private final String name;
    private final Long size;
    private final String ContentType;
    private final LocalDateTime uploadDate;
    private final Long folderId;
    private final String FolderName;
    private final String downloadUrl;

    public FileResponseDTO(Long id, String name, Long size, String contentType, LocalDateTime uploadDate, Long folderId, String folderName, String downloadUrl) {
        this.id = id;
        this.name = name;
        this.size = size;
        ContentType = contentType;
        this.uploadDate = uploadDate;
        this.folderId = folderId;
        FolderName = folderName;
        this.downloadUrl = downloadUrl;
        addHateoasLinks();
    }

    private void addHateoasLinks() {
        add(linkTo(methodOn(FileController.class).downloadFile(id, null, null))
                .withRel("download"));

        add(linkTo(methodOn(FileController.class).deleteFile(id))
                .withRel("delete"));

        if (folderId != null) {
            add(linkTo(methodOn(FolderController.class).getFolderById(id))
                    .withRel("folder"));
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public String getContentType() {
        return ContentType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public Long getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return FolderName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
