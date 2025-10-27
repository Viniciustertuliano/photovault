package com.PhotoVault.repository;

import com.PhotoVault.entities.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
}
