package com.PhotoVault.dto.response;

import com.PhotoVault.controller.PublicShareController;
import com.PhotoVault.controller.ShareLinkController;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareLinkResponseDTO extends RepresentationModel<ShareLinkResponseDTO> {

    private final Long id;
    private final String token;
    private final String shareUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime expirationDate;
    private final Boolean active;
    private final Integer accessCount;
    private final Long folderId;
    private final String folderName;

    public ShareLinkResponseDTO(Long id, String token, String shareUrl, LocalDateTime createdAt, LocalDateTime expirationDate, Boolean active, Integer accessCount, Long folderId, String folderName) {
        this.id = id;
        this.token = token;
        this.shareUrl = shareUrl;
        this.createdAt = createdAt;
        this.expirationDate = expirationDate;
        this.active = active;
        this.accessCount = accessCount;
        this.folderId = folderId;
        this.folderName = folderName;
        addHateoasLinks();
    }

    private void addHateoasLinks() {

        add(linkTo(methodOn(ShareLinkController.class).getShareLinksByFolder(id))
                .withSelfRel());

        if (active && token != null) {
            add(linkTo(methodOn(PublicShareController.class).accessSharedFolder(token, null))
                    .withRel("public-access"));
        }

        if (active){
            add(linkTo(methodOn(ShareLinkController.class).revokeShareLink(id))
                    .withRel("revoke"));
        }

        add(linkTo(methodOn(ShareLinkController.class).renewShareLink(id, null))
                .withRel("renew"));
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public Boolean getActive() {
        return active;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public Long getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return folderName;
    }
}
