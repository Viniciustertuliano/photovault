package com.PhotoVault.services;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.response.ClientResponseDTO;
import com.PhotoVault.entities.Client;
import com.PhotoVault.entities.UserRole;
import com.PhotoVault.exception.EmailAlreadyExistsException;
import com.PhotoVault.exception.ResourceNotFoundException;
import com.PhotoVault.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private ClientResponseDTO toResponseDTO (Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getRole()
        );
    }

    public ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO) {
        if (clientRepository.findByEmail(clientRequestDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        Client client = new Client();
        client.setName(clientRequestDTO.getName());
        client.setEmail(clientRequestDTO.getEmail());
        client.setPhoneNumber(clientRequestDTO.getPhoneNumber());
        client.setPassword(passwordEncoder.encode(clientRequestDTO.getPassword()));
        client.setRole(UserRole.CLIENT);

        return toResponseDTO(clientRepository.save(client));
    }

    public List<ClientResponseDTO> findAll(){
        return clientRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ClientResponseDTO findById(Long id){
        Client client = clientRepository.
                findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return toResponseDTO(client);
    }

    public ClientResponseDTO findByEmail(String email){
        Client client = clientRepository.
                findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException("Client", "Email", email));

        return toResponseDTO(client);
    }

    public ClientResponseDTO updateClient(Long id, ClientRequestDTO clientRequestDTO){
        Client client = clientRepository.
                findById(id).
                orElseThrow(
                        () -> new ResourceNotFoundException("Client", id)
                );

        if (!client.getEmail().equals(clientRequestDTO.getEmail())) {
            clientRepository.findByEmail(clientRequestDTO.getEmail()).ifPresent(existing -> {
                throw new EmailAlreadyExistsException("Email already exists!", clientRequestDTO.getEmail());
            });
        }

        if (clientRequestDTO.getPassword() != null && !clientRequestDTO.getPassword().isEmpty()){
            client.setPassword(passwordEncoder.encode(clientRequestDTO.getPassword()));
        }

        client.setName(clientRequestDTO.getName());
        client.setEmail(clientRequestDTO.getEmail());
        client.setPhoneNumber(clientRequestDTO.getPhoneNumber());

        return  toResponseDTO(clientRepository.save(client));
    }

    public void deleteClient(Long id){
        Client client = clientRepository.
                findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Client"));
        clientRepository.delete(client);
    }
}
