package com.PhotoVault.services;

import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.PhotographerResponseDTO;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import com.PhotoVault.exception.EmailAlreadyExistsException;
import com.PhotoVault.exception.ResourceNotFoundException;
import com.PhotoVault.repository.PhotographerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotographerService {

    private final PhotographerRepository photographerRepository;
    private final PasswordEncoder passwordEncoder;

    public PhotographerService(PhotographerRepository photographerRepository, PasswordEncoder passwordEncoder) {
        this.photographerRepository = photographerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private PhotographerResponseDTO toResponseDTO(Photographer photographer) {
        return new PhotographerResponseDTO(
                photographer.getId(),
                photographer.getName(),
                photographer.getEmail(),
                photographer.getRole()
        );
    }

    public PhotographerResponseDTO createPhotographer(PhotographerRequestDTO photographerRequestDTO) {
        if (photographerRepository.findByEmail(photographerRequestDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        Photographer photographer = new Photographer();
        photographer.setName(photographerRequestDTO.getName());
        photographer.setEmail(photographerRequestDTO.getEmail());
        photographer.setPassword(passwordEncoder.encode(photographerRequestDTO.getPassword()));
        photographer.setRole(UserRole.PHOTOGRAPHER);

        return toResponseDTO(photographerRepository.save(photographer));

    }

    public PhotographerResponseDTO findById(Long id){
        Photographer photographer = photographerRepository.
                findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Photographer", id));
        return toResponseDTO(photographer);
    }

    public PhotographerResponseDTO findByEmail(String email){
        Photographer photographer = photographerRepository.
                findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException("Photographer", "Email", email));

        return toResponseDTO(photographer);
    }

    public List<PhotographerResponseDTO> findAll(){
        return photographerRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PhotographerResponseDTO updatePhotographer(Long id, PhotographerRequestDTO photographerRequestDTO){
        Photographer photographer = photographerRepository.
                findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Photographer not found!", id));

        if (!photographer.getEmail().equals(photographerRequestDTO.getEmail())) {
            photographerRepository.findByEmail(photographerRequestDTO.getEmail()).ifPresent(existing -> {
                throw new EmailAlreadyExistsException("Email already exists!", photographerRequestDTO.getEmail());
            });
        }

        if (photographerRequestDTO.getPassword() != null && !photographerRequestDTO.getPassword().isEmpty()){
            photographer.setPassword(passwordEncoder.encode(photographerRequestDTO.getPassword()));
        }

        photographer.setName(photographerRequestDTO.getName());
        photographer.setEmail(photographerRequestDTO.getEmail());

        return toResponseDTO(photographerRepository.save(photographer));
    }

    public void deletePhotographer(Long id){
        Photographer photographer = photographerRepository.
                findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Photographer"));

        photographerRepository.delete(photographer);
    }
}
