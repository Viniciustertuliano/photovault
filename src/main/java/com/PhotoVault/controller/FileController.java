package com.PhotoVault.controller;


import com.PhotoVault.dto.response.FileResponseDTO;
import com.PhotoVault.entities.File;
import com.PhotoVault.exception.ForbiddenException;
import com.PhotoVault.services.FileService;
import com.PhotoVault.services.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/")
@Tag(name = "File Controller", description = "Endpoints for managing files")
@SecurityRequirement(name = "bearer-jwt")
public class FileController {

    private final FileService fileService;
    private final ShareLinkService shareLinkService;

    public FileController(FileService fileService, ShareLinkService shareLinkService) {
        this.fileService = fileService;
        this.shareLinkService = shareLinkService;
    }

    @PostMapping("/folders/{folderId}/files")
    @Operation(summary = "Upload a file to a folder", description = "Uploads a file to the specified folder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<FileResponseDTO> uploadFile(
            @Parameter(description = "ID of the folder to upload the file to") @PathVariable Long folderId,
            @Parameter(description = "File to be uploaded") @RequestParam("file") MultipartFile file
            ){
        FileResponseDTO response = fileService.uploadFile(folderId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/folders/{folderId}/files")
    @Operation(
            summary = "Get files by folder ID",
            description = "Retrieves all files within the specified folder."
    )
    public ResponseEntity<List<FileResponseDTO>> getFilesByFolder(@Parameter(description = "ID of the folder") @PathVariable Long folderId){

        return ResponseEntity.ok(fileService.getFilesByFolder(folderId));
    }

    @GetMapping("file/{id}")
    @Operation(summary = "Download a file by ID", description = "Downloads the file with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "ID of the file to download") @PathVariable Long id,
            @RequestParam(value = "shareToken", required = false) String shareToken,
            Authentication authentication){

        boolean hasShareToken = shareToken != null && !shareToken.isEmpty();
        boolean hasAuthentication = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        if (hasShareToken) {
            shareLinkService.validateTokenForFile(shareToken, id);
        } else if (hasAuthentication) {
            fileService.validateAccessForFile(shareToken, id);

        }else {
            throw new ForbiddenException("Access denied. Provide a valid shareToken or authenticate.");
        }

        Resource resource = fileService.downloadFile(id);
        File fileEntity = fileService.getFileEntity(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                .body(resource);
    }

    @DeleteMapping("file/{id}")
    @Operation(summary = "Delete a file by ID", description = "Deletes the file with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Void> deleteFile(@Parameter(description = "ID of the file to delete") @PathVariable Long id){
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }


}
