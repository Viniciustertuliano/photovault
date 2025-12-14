package com.PhotoVault.security;

import com.PhotoVault.entities.Client;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.repository.ClientRepository;
import com.PhotoVault.repository.PhotographerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PhotographerRepository photographerRepository;
    private final ClientRepository clientRepository;

    public CustomUserDetailsService(PhotographerRepository photographerRepository, ClientRepository clientRepository) {
        this.photographerRepository = photographerRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Photographer> photographer = photographerRepository.findByEmail(email);
        if (photographer.isPresent()){
            return buildUserDetails(
                    photographer.get().getEmail(),
                    photographer.get().getPassword(),
                    photographer.get().getRole().name()
            );
        }

        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()){
            return buildUserDetails(
                    client.get().getEmail(),
                    client.get().getPassword(),
                    client.get().getRole().name()
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    private UserDetails buildUserDetails( String email, String password, String role){
        return User.builder()
                .username(email)
                .password(password)
                .roles(role)
                .build();
    }
}
