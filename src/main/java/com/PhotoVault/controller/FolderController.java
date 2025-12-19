package com.PhotoVault.controller;

import com.PhotoVault.dto.request.FolderRequestDTO;
import com.PhotoVault.dto.response.FolderResponseDTO;
import com.PhotoVault.services.FolderService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/folders")
@Tag(name = "Folder Controller", description = "Endpoints for managing folders")
@SecurityRequirement(name = "bearer-jwt")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    @Operation(summary = "Create a new folder", description = "Creates a new folder for the authenticated photographer.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Folder created successfully",content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<FolderResponseDTO> createFolder(@Valid @RequestBody FolderRequestDTO folderRequest) {
        FolderResponseDTO response = folderService.createFolder(folderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get folder by ID", description = "Retrieves a folder by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Folder retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Folder not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<FolderResponseDTO> getFolderById(@PathVariable Long id){
        return ResponseEntity.ok(folderService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all folders", description = "Retrieves all folders.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Folders retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<FolderResponseDTO>> getAllFolders(){
        return ResponseEntity.ok(folderService.findAll());
    }

    @GetMapping("/owner")
    @Operation(summary = "Get folders by owner ID", description = "Retrieves all folders owned by the authenticated photographer.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Folders retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<FolderResponseDTO>> getAllByOwnerId(){
        return ResponseEntity.ok(folderService.findAllByOwnerId());
    }

    @GetMapping("/photographer/{photographerId}")
    @Operation(summary = "Get folders by photographer ID", description = "Retrieves all folders for a specific photographer by their ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Folders retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Photographer not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<FolderResponseDTO>> getFoldersByPhotographerId(@PathVariable Long photographerId){
        return ResponseEntity.ok(folderService.findAllByPhotographerId(photographerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update folder", description = "Updates the details of an existing folder.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Folder updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Folder not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<FolderResponseDTO> updateFolder(@PathVariable Long id, @Valid @RequestBody FolderRequestDTO folderRequest){
        return ResponseEntity.ok(folderService.updateFolder(id, folderRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete folder", description = "Deletes a folder by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Folder deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Folder not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id){
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}
