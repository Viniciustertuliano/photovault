package com.PhotoVault.controller;

import com.PhotoVault.dto.response.FileResponseDTO;
import com.PhotoVault.dto.response.FolderAccessDTO;
import com.PhotoVault.services.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/share")
@Tag(name = "Public Share Access", description = "Public endpoints for accessing shared content")
public class PublicShareController {

    private final ShareLinkService shareLinkService;

    public PublicShareController(ShareLinkService shareLinkService) {
        this.shareLinkService = shareLinkService;
    }

    @GetMapping("/{token}")
    @Operation(
            summary = "Access shared folder",
            description = "Access folder contents using a share token. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder accessed successfully"),
            @ApiResponse(responseCode = "403", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "404", description = "Shared folder not found")
    })
    public ResponseEntity<FolderAccessDTO> accessSharedFolder(
            @Parameter(description = "Share token")
            @PathVariable String token,
            @ParameterObject Pageable pageable){

        return ResponseEntity.ok(shareLinkService.accessFolderByToken(token, pageable));
    }
}
