package com.PhotoVault.controller;

import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.PhotographerResponseDTO;
import com.PhotoVault.services.PhotographerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/photographers")
public class PhotographerController {

    private final PhotographerService photographerService;

    public PhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    @PostMapping
    public ResponseEntity<PhotographerResponseDTO> createPhotographer(@Valid @RequestBody PhotographerRequestDTO photographerReq) {
        PhotographerResponseDTO photographerRes = photographerService.createPhotographer(photographerReq);
        URI location = URI.create(String.format("/api/photographers/%s", photographerRes.getId()));
        return ResponseEntity.created(location).body(photographerRes);
    }

    @GetMapping
    public ResponseEntity<List<PhotographerResponseDTO>> getAllPhotographers() {
        return ResponseEntity.ok(
                photographerService.findAll()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<PhotographerResponseDTO> findPhotographerById(@PathVariable Long id){
        return ResponseEntity.ok(
                photographerService.findById(id)
        );
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<PhotographerResponseDTO> findPhotographerByEmail(@PathVariable String email){
        return ResponseEntity.ok(
                photographerService.findByEmail(email)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotographerResponseDTO> UpdatePhotographer(
                                                                    @PathVariable Long id,
                                                                    @Valid @RequestBody PhotographerRequestDTO photographerRequestDTO){
        return ResponseEntity.ok(
                photographerService.updatePhotographer(id, photographerRequestDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhotographer(@PathVariable Long id){
        photographerService.deletePhotographer(id);
        return ResponseEntity.noContent().build();
    }
}
