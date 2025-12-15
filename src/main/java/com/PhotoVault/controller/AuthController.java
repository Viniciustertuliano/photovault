package com.PhotoVault.controller;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.request.LoginRequestDTO;
import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.AuthResult;
import com.PhotoVault.dto.response.ClientAuthResponseDTO;
import com.PhotoVault.dto.response.PhotographerAuthResponseDTO;
import com.PhotoVault.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/photographer")
    public ResponseEntity<PhotographerAuthResponseDTO> registerPhotographer(
            @Valid @RequestBody PhotographerRequestDTO photographerRequest){
        PhotographerAuthResponseDTO authResponseDTO = authService.registerPhotographer(photographerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }

    @PostMapping("/register/client")
    public ResponseEntity<ClientAuthResponseDTO> registerclient(
            @Valid @RequestBody ClientRequestDTO clientRequest){
        ClientAuthResponseDTO authResponseDTO = authService.registerClient(clientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(
            @Valid @RequestBody LoginRequestDTO loginRequest){
        AuthResult authResul = authService.login(loginRequest);
        return ResponseEntity.ok(authResul);
    }

}
