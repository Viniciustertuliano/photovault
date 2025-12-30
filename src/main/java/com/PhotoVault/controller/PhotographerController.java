package com.PhotoVault.controller;

import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.PhotographerResponseDTO;
import com.PhotoVault.services.PhotographerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/photographers")
@Tag(name = "Photographer Controller", description = "Endpoints for managing photographers")
@SecurityRequirement(name = "bearer-jwt")
public class PhotographerController {

    private final PhotographerService photographerService;

    public PhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    @PostMapping
    @Operation(summary = "Create a new photographer", description = "Creates a new photographer with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photographer created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PhotographerResponseDTO> createPhotographer(@Valid @RequestBody PhotographerRequestDTO photographerReq) {
        PhotographerResponseDTO photographerRes = photographerService.createPhotographer(photographerReq);
        URI location = URI.create(String.format("/api/photographers/%s", photographerRes.getId()));
        return ResponseEntity.created(location).body(photographerRes);
    }

    @GetMapping
    @Operation(summary = "Get all photographers", description = "Retrieves all photographers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photographers retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<PhotographerResponseDTO>> getAllPhotographers() {
        return ResponseEntity.ok(
                photographerService.findAll()
        );
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get photographer by ID", description = "Retrieves a photographer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photographer retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Photographer not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PhotographerResponseDTO> findPhotographerById(@PathVariable Long id){
        return ResponseEntity.ok(
                photographerService.findById(id)
        );
    }
    @GetMapping("/email/{email}")
    @Operation(summary = "Get photographer by email", description = "Retrieves a photographer by their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photographer retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Photographer not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PhotographerResponseDTO> findPhotographerByEmail(@PathVariable String email){
        return ResponseEntity.ok(
                photographerService.findByEmail(email)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update photographer", description = "Updates the details of an existing photographer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photographer updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Photographer not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PhotographerResponseDTO> UpdatePhotographer(
                                                                    @PathVariable Long id,
                                                                    @Valid @RequestBody PhotographerRequestDTO photographerRequestDTO){
        return ResponseEntity.ok(
                photographerService.updatePhotographer(id, photographerRequestDTO)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete photographer", description = "Deletes a photographer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photographer deleted successfully", content =  @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Photographer not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> deletePhotographer(@PathVariable Long id){
        photographerService.deletePhotographer(id);
        return ResponseEntity.noContent().build();
    }
}
