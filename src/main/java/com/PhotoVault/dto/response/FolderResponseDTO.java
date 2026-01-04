package com.PhotoVault.dto.response;

import com.PhotoVault.controller.FileController;
import com.PhotoVault.controller.FolderController;
import com.PhotoVault.controller.ShareLinkController;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderResponseDTO extends RepresentationModel<FolderResponseDTO> {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final PhotographerSummaryDTO owner;

    public FolderResponseDTO(Long id, String name, LocalDateTime createdAt, PhotographerSummaryDTO owner) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.owner = owner;
        addHateoasLinks();
    }

    private void addHateoasLinks() {
        add(linkTo(methodOn(FolderController.class).getFolderById(id))
                .withSelfRel());

        add(linkTo(methodOn(FileController.class).getFilesByFolder(id, null))
                .withRel("files"));

        add(linkTo(methodOn(FolderController.class).updateFolder(id, null))
                .withRel("update"));

        add(linkTo(methodOn(FolderController.class).deleteFolder(id))
                .withRel("delete"));

        add(linkTo(methodOn(ShareLinkController.class).createShareLink(null, null))
                .withRel("create-share-link"));

        if (owner != null) {
            add(linkTo(methodOn(FolderController.class).getFoldersByPhotographerId(owner.getId(), null))
                    .withRel("owner-folders"));
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PhotographerSummaryDTO getOwner() {
        return owner;
    }
}
