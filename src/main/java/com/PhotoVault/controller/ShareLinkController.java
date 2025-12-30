package com.PhotoVault.controller;

import com.PhotoVault.dto.request.ShareLinkRequestDTO;
import com.PhotoVault.dto.response.FolderAccessDTO;
import com.PhotoVault.dto.response.ShareLinkResponseDTO;
import com.PhotoVault.services.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "ShareLinks Controller", description = "Endpoints for managing share links")
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    public ShareLinkController(ShareLinkService shareLinkService) {
        this.shareLinkService = shareLinkService;
    }

    @PostMapping("/folder/{folderId}/share-links")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Share link created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareLinkResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ShareLinkResponseDTO> createShareLink(
            @Parameter(description = "ID of the folder to create a share link")
            @PathVariable Long folderId,
            @Valid @RequestBody ShareLinkRequestDTO request
            ) {

        ShareLinkResponseDTO response = shareLinkService.createShareLink(folderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/share-links/{token}/access")
    @Operation(summary = "Access folder by share link token",
            description = "Accesses a folder using the provided share link token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder accessed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FolderAccessDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Share link is inactive",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Share link not found or expired",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<FolderAccessDTO> accessFolderByToken(
            @Parameter(description = "Share link token")
            @PathVariable String token){

        FolderAccessDTO response = shareLinkService.accessFolderByToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/folders/{folderId}/share-links")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Get share links by folder ID",
            description = "Retrieves all share links associated with the specified folder."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Share links retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareLinkResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<ShareLinkResponseDTO>> getShareLinksByFolder(
            @Parameter(description = "ID of the folder to get share links for")
            @PathVariable Long folderId){

        return ResponseEntity.status(HttpStatus.OK).body(shareLinkService.getShareLinksByFolder(folderId));
    }

    @DeleteMapping("/share-links/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Revoke a share link", description = "Revokes the share link with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Share link revoked successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Share link not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> revokeShareLink(@Parameter(description = "ID of the share link to revoke") @PathVariable Long id){
        shareLinkService.revokeShareLink(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/share-links/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Renew a share link", description = "Renews the share link with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Share link renewed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareLinkResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Share link not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ShareLinkResponseDTO> renewShareLink(
            @Parameter(description = "ID of the share link to renew") @PathVariable Long id,
            @Parameter(description = "Additional days") @RequestParam(defaultValue = "7") Integer additionalDays){

        ShareLinkResponseDTO response = shareLinkService.renewShareLink(id, additionalDays);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
