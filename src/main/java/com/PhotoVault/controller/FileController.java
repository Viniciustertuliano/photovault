package com.PhotoVault.controller;


import com.PhotoVault.dto.request.FileUpdateDTO;
import com.PhotoVault.dto.response.FileResponseDTO;
import com.PhotoVault.entities.File;
import com.PhotoVault.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api")
@Tag(name = "File Controller", description = "Endpoints for managing files")
@SecurityRequirement(name = "bearer-jwt")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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
    public ResponseEntity<Page<FileResponseDTO>> getFilesByFolder(
            @Parameter(description = "ID of the folder")
            @PathVariable Long folderId,
            @PageableDefault(size = 20, sort = "uploadDate")Pageable pageable){

        return ResponseEntity.ok(fileService.getFilesByFolder(folderId, pageable));
    }

    @GetMapping("/files/{id}/view")
    @Operation(summary = "View a file by ID", description = "Renders the file in the browser. Requires VIEW permission.")
    public ResponseEntity<Resource> viewFile(@PathVariable("id") Long fileId) {
        Resource resource = fileService.viewFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/files/{id}/download")
    @Operation(summary = "Download a file by ID", description = "Downloads the file to the user's device. Requires DOWNLOAD permission.")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long fileId) {
        Resource resource = fileService.downloadFile(fileId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("files/{id}")
    @Operation(summary = "Download a file by ID", description = "Downloads the file with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "ID of the file to download") @PathVariable("id") Long fileId,
            @RequestParam(value = "shareToken", required = false) String shareToken,
            Authentication authentication){


        Resource resource = fileService.downloadFile(fileId);
        File fileEntity = fileService.getFileEntity(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                .body(resource);
    }

    @PatchMapping("/files/{id}")
    @Operation(summary = "Rename or move a file", description = "Partially updates a file's name or its destination folder.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Forbidden to modify this file or target folder"),
        @ApiResponse(responseCode = "404", description = "File or target folder not found")
    })
    public ResponseEntity<FileResponseDTO> updateFile(
        @Parameter(description = "ID of the file to update") @PathVariable("id") Long id,
        @Valid @RequestBody FileUpdateDTO updateDTO){

            FileResponseDTO responseDTO = fileService.updateFile(id, updateDTO);
            return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/files/{id}")
    @Operation(summary = "Delete a file by ID", description = "Deletes the file with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Void> deleteFile(@Parameter(description = "ID of the file to delete") @PathVariable("id") Long id){
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }


}
