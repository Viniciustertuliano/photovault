package com.PhotoVault.repository;

import com.PhotoVault.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    public Optional<Client> findByEmail(String email);
}
