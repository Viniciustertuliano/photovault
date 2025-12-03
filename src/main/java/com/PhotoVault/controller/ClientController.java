package com.PhotoVault.controller;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.response.ClientResponseDTO;
import com.PhotoVault.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController  {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO clientReqDTO) {
        ClientResponseDTO clientResp = clientService.createClient(clientReqDTO);
        URI location = URI.create(String.format("/api/clients/%s", clientResp.getId()));
        return ResponseEntity.created(location).body(clientResp);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> findAllClients() {
        return ResponseEntity.ok(
                clientService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> findClientById(@PathVariable Long id) {
        return ResponseEntity.ok(
                clientService.findById(id)
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDTO> findClientByEmail(@PathVariable String email){
        return ResponseEntity.ok(
                clientService.findByEmail(email)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO clientReqDTO){
        return ResponseEntity.ok(
                clientService.updateClient(id, clientReqDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }


}
