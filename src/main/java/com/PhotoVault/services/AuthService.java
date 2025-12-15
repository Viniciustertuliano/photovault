package com.PhotoVault.services;

import com.PhotoVault.dto.request.ClientRequestDTO;
import com.PhotoVault.dto.request.LoginRequestDTO;
import com.PhotoVault.dto.request.PhotographerRequestDTO;
import com.PhotoVault.dto.response.*;
import com.PhotoVault.entities.Client;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import com.PhotoVault.exception.EmailAlreadyExistsException;
import com.PhotoVault.repository.ClientRepository;
import com.PhotoVault.repository.PhotographerRepository;
import com.PhotoVault.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final PhotographerRepository photographerRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(PhotographerRepository photographerRepository, ClientRepository clientRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.photographerRepository = photographerRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    private PhotographerResponseDTO photographerToResponseDTO(Photographer photographer) {
        return new PhotographerResponseDTO(
                photographer.getId(),
                photographer.getName(),
                photographer.getEmail(),
                photographer.getRole()
        );
    }

    private ClientResponseDTO clientToResponseDTO (Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getRole()
        );
    }

    public PhotographerAuthResponseDTO registerPhotographer(PhotographerRequestDTO photographerRequestDTO) {
        if (photographerRepository.findByEmail(photographerRequestDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(photographerRequestDTO.getEmail());
        }
        Photographer photographer = new Photographer();
        photographer.setName(photographerRequestDTO.getName());
        photographer.setEmail(photographerRequestDTO.getEmail());
        photographer.setPassword(passwordEncoder.encode(photographerRequestDTO.getPassword()));
        photographer.setRole(UserRole.PHOTOGRAPHER);

        Photographer savedPhotographer = photographerRepository.save(photographer);

        String token = jwtTokenProvider.generateToken(savedPhotographer.getEmail());

        return new PhotographerAuthResponseDTO(token,"Bearer", photographerToResponseDTO(savedPhotographer));
    }

    public ClientAuthResponseDTO registerClient(ClientRequestDTO clientRequest){
        if (clientRepository.findByEmail(clientRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(clientRequest.getEmail());
        }

        Client client = new Client();
        client.setName(clientRequest.getName());
        client.setEmail(clientRequest.getEmail());
        client.setPhoneNumber(clientRequest.getPhoneNumber());
        client.setPassword(passwordEncoder.encode(clientRequest.getPassword()));
        client.setRole(UserRole.CLIENT);

        Client savedClient = clientRepository.save(client);

        String token = jwtTokenProvider.generateToken(savedClient.getEmail());

        return new ClientAuthResponseDTO(token, "Bearer",clientToResponseDTO(savedClient));

    }

    public AuthResult login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        Optional<Photographer> photographer = photographerRepository.findByEmail(loginRequest.getEmail());
        if (photographer.isPresent()){
            String token = jwtTokenProvider.generateToken(photographer.get().getEmail());
            return new PhotographerAuthResponseDTO(token,"Bearer" ,photographerToResponseDTO(photographer.get()));
        }

        Client client = clientRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        String token = jwtTokenProvider.generateToken(client.getEmail());

        return new ClientAuthResponseDTO(token, "Bearer", clientToResponseDTO(client));
    }
}
