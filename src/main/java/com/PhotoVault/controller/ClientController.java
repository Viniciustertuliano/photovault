package com.PhotoVault.controller;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.response.ClientResponseDTO;
import com.PhotoVault.services.ClientService;
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
@RequestMapping("/api/clients")
@Tag(name = "Client Controller", description = "Endpoints for managing clients")
@SecurityRequirement(name = "bearer-jwt")
public class ClientController  {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(summary = "Create a new client", description = "Creates a new client with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Client created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO clientReqDTO) {
        ClientResponseDTO clientResp = clientService.createClient(clientReqDTO);
        URI location = URI.create(String.format("/api/clients/%s", clientResp.getId()));
        return ResponseEntity.created(location).body(clientResp);
    }

    @GetMapping
    @Operation(summary = "Get all clients", description = "Retrieves all clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clients retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<ClientResponseDTO>> findAllClients() {
        return ResponseEntity.ok(
                clientService.findAll()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieves a client by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ClientResponseDTO> findClientById(@PathVariable Long id) {
        return ResponseEntity.ok(
                clientService.findById(id)
        );
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get client by email", description = "Retrieves a client by their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ClientResponseDTO> findClientByEmail(@PathVariable String email){
        return ResponseEntity.ok(
                clientService.findByEmail(email)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client", description = "Updates the details of an existing client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO clientReqDTO){
        return ResponseEntity.ok(
                clientService.updateClient(id, clientReqDTO)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client", description = "Deletes a client by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Client deleted successfully", content =  @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


}
