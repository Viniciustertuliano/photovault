package com.PhotoVault.repository;

import com.PhotoVault.entities.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {

    public Optional<Photographer> findByEmail(String email);

}
