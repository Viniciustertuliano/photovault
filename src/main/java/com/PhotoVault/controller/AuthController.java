package com.PhotoVault.controller;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.request.LoginRequestDTO;
import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.AuthResult;
import com.PhotoVault.dto.response.ClientAuthResponseDTO;
import com.PhotoVault.dto.response.PhotographerAuthResponseDTO;
import com.PhotoVault.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/photographer")
    @Operation(summary = "Register a new photographer", description = "Creates a new photographer account with the provided details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Photographer registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PhotographerAuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PhotographerAuthResponseDTO> registerPhotographer(
            @Valid @RequestBody PhotographerRequestDTO photographerRequest){
        PhotographerAuthResponseDTO authResponseDTO = authService.registerPhotographer(photographerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }

    @PostMapping("/register/client")
    @Operation(summary = "Register a new client", description = "Creates a new client account with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Client registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientAuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ClientAuthResponseDTO> registerClient(
            @Valid @RequestBody ClientRequestDTO clientRequest){
        ClientAuthResponseDTO authResponseDTO = authService.registerClient(clientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns an JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<AuthResult> login(
            @Valid @RequestBody LoginRequestDTO loginRequest){
        AuthResult authResul = authService.login(loginRequest);
        return ResponseEntity.ok(authResul);
    }

}
